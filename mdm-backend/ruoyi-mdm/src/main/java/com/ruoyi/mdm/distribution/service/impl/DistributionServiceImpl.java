package com.ruoyi.mdm.distribution.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.mdm.distribution.domain.MdmApp;
import com.ruoyi.mdm.distribution.domain.MdmDistribution;
import com.ruoyi.mdm.distribution.domain.MdmDistributionRecord;
import com.ruoyi.mdm.distribution.mapper.MdmAppMapper;
import com.ruoyi.mdm.distribution.mapper.MdmDistributionMapper;
import com.ruoyi.mdm.distribution.mapper.MdmDistributionRecordMapper;
import com.ruoyi.mdm.distribution.service.IDistributionService;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

/**
 * 主数据分发与集成 服务层实现
 *
 * <p>内嵌分发器：变更即推默认异步线程推送（复用若依 AsyncManager），结果写分发记录表；
 * 失败可通过记录重推。订阅方不可用只影响该次推送，不回滚主流程。
 *
 * @author ruoyi
 */
@Service
public class DistributionServiceImpl implements IDistributionService
{
    @Autowired
    private MdmAppMapper appMapper;

    @Autowired
    private MdmDistributionMapper distMapper;

    @Autowired
    private MdmDistributionRecordMapper recordMapper;

    @Autowired
    private MdmObjectMapper objectMapper;

    /** 异步推送线程池（daemon，订阅方不可用不阻塞主流程） */
    // ponytail: 固定小线程池，订阅方规模扩大或推送量大时按设计决策五引入 RabbitMQ 替换
    private static final ExecutorService PUSH_EXECUTOR = Executors.newFixedThreadPool(2, r ->
    {
        Thread t = new Thread(r, "mdm-distribution-push");
        t.setDaemon(true);
        return t;
    });

    // ===== 应用凭证 =====

    @Override
    public List<MdmApp> listApp(MdmApp mdmApp)
    {
        return appMapper.selectAppList(mdmApp);
    }

    @Override
    public MdmApp getApp(Long appId)
    {
        return appMapper.selectAppById(appId);
    }

    @Override
    public MdmApp addApp(MdmApp mdmApp)
    {
        if (StringUtils.isEmpty(mdmApp.getAppName()))
        {
            throw new ServiceException("应用名称不能为空");
        }
        mdmApp.setAppid("app_" + IdUtils.fastSimpleUUID().substring(0, 16));
        mdmApp.setSecret("sk_" + IdUtils.fastSimpleUUID());
        // 新增订阅方默认启用，便于立即接入
        mdmApp.setEnabled(StringUtils.isEmpty(mdmApp.getEnabled()) ? "1" : mdmApp.getEnabled());
        mdmApp.setCreateBy(SecurityUtils.getUsername());
        appMapper.insertApp(mdmApp);
        return mdmApp;
    }

    @Override
    public MdmApp getAppByAppid(String appid)
    {
        return appMapper.selectAppByAppid(appid);
    }

    @Override
    public int editApp(MdmApp mdmApp)
    {
        mdmApp.setUpdateBy(SecurityUtils.getUsername());
        return appMapper.updateApp(mdmApp);
    }

    @Override
    public int deleteApps(Long[] appIds)
    {
        return appMapper.deleteAppByIds(appIds);
    }

    @Override
    public String resetSecret(Long appId)
    {
        MdmApp app = appMapper.selectAppById(appId);
        if (app == null)
        {
            throw new ServiceException("应用凭证不存在");
        }
        String secret = "sk_" + IdUtils.fastSimpleUUID();
        MdmApp update = new MdmApp();
        update.setAppId(appId);
        update.setSecret(secret);
        update.setUpdateBy(SecurityUtils.getUsername());
        appMapper.updateSecret(update);
        return secret;
    }

    // ===== 分发配置 =====

    @Override
    public List<MdmDistribution> listDist(MdmDistribution mdmDistribution)
    {
        return distMapper.selectDistList(mdmDistribution);
    }

    @Override
    public MdmDistribution getDist(Long distId)
    {
        return distMapper.selectDistById(distId);
    }

    @Override
    public int addDist(MdmDistribution mdmDistribution)
    {
        mdmDistribution.setTriggerType(StringUtils.isEmpty(mdmDistribution.getTriggerType()) ? "IMMEDIATE" : mdmDistribution.getTriggerType());
        mdmDistribution.setEnabled(StringUtils.isEmpty(mdmDistribution.getEnabled()) ? "1" : mdmDistribution.getEnabled());
        mdmDistribution.setCreateBy(SecurityUtils.getUsername());
        return distMapper.insertDist(mdmDistribution);
    }

    @Override
    public int editDist(MdmDistribution mdmDistribution)
    {
        mdmDistribution.setUpdateBy(SecurityUtils.getUsername());
        return distMapper.updateDist(mdmDistribution);
    }

    @Override
    public int deleteDists(Long[] distIds)
    {
        return distMapper.deleteDistByIds(distIds);
    }

    // ===== 分发记录 =====

    @Override
    public List<MdmDistributionRecord> listRecord(MdmDistributionRecord mdmDistributionRecord)
    {
        return recordMapper.selectRecordList(mdmDistributionRecord);
    }

    @Override
    public int retryRecord(Long recordId)
    {
        MdmDistributionRecord record = recordMapper.selectRecordById(recordId);
        if (record == null)
        {
            throw new ServiceException("分发记录不存在");
        }
        // 重推优先使用当前配置回调地址（如订阅方已修好），而非记录里的历史死地址
        MdmObject object = objectMapper.checkObjectCodeUnique(record.getObjectCode());
        if (object != null)
        {
            for (MdmDistribution cfg : distMapper.selectEnabledListByObjectId(object.getObjectId()))
            {
                if (cfg.getAppId().equals(record.getAppId()) && StringUtils.isNotEmpty(cfg.getEndpointUrl()))
                {
                    record.setEndpointUrl(cfg.getEndpointUrl());
                    break;
                }
            }
        }
        // 手动重推：同步等待结果，方便前端直接反馈
        return send(record, true) ? 1 : 0;
    }

    @Override
    public int confirmRecord(Long recordId)
    {
        if (recordMapper.selectRecordById(recordId) == null)
        {
            throw new ServiceException("分发记录不存在");
        }
        MdmDistributionRecord confirm = new MdmDistributionRecord();
        confirm.setRecordId(recordId);
        return recordMapper.updateConfirm(confirm);
    }

    // ===== 内嵌分发器 =====

    @Override
    public void triggerPush(String objectCode, Long dataId, String actionType, Map<String, Object> data)
    {
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (object == null)
        {
            return;
        }
        List<MdmDistribution> configs = distMapper.selectEnabledListByObjectId(object.getObjectId());
        if (configs == null || configs.isEmpty())
        {
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("objectCode", objectCode);
        body.put("dataId", dataId);
        body.put("actionType", actionType);
        body.put("actionTime", DateUtils.dateTimeNow());
        body.put("data", data);
        String payload = JSON.toJSONString(body);
        for (MdmDistribution config : configs)
        {
            MdmDistributionRecord record = new MdmDistributionRecord();
            record.setAppId(config.getAppId());
            record.setObjectCode(objectCode);
            record.setDataId(dataId == null ? 0L : dataId);
            record.setActionType(actionType);
            record.setEndpointUrl(config.getEndpointUrl());
            record.setPayload(payload);
            record.setStatus("0");
            record.setCreateBy(SecurityUtils.getUsername());
            recordMapper.insertRecord(record);
            // 异步推送，订阅方不可用不阻塞主流程
            PUSH_EXECUTOR.submit(() -> send(record, false));
        }
    }

    /**
     * 推送一条记录并回写结果
     *
     * @param sync 同步（手动重推）或异步
     * @return 是否成功
     */
    private boolean send(MdmDistributionRecord record, boolean sync)
    {
        MdmDistributionRecord sending = new MdmDistributionRecord();
        sending.setRecordId(record.getRecordId());
        sending.setSendTime(DateUtils.dateTimeNow());
        recordMapper.updatePushResult(sending);
        try
        {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);
            RestTemplate restTemplate = new RestTemplate(factory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(record.getEndpointUrl(),
                    new HttpEntity<>(record.getPayload(), headers), String.class);
            MdmDistributionRecord ok = new MdmDistributionRecord();
            ok.setRecordId(record.getRecordId());
            ok.setStatus("1");
            ok.setSuccessTime(DateUtils.dateTimeNow());
            recordMapper.updatePushResult(ok);
            return true;
        }
        catch (Exception e)
        {
            MdmDistributionRecord fail = new MdmDistributionRecord();
            fail.setRecordId(record.getRecordId());
            fail.setStatus("2");
            String msg = StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
            fail.setErrorMsg(msg.length() > 490 ? msg.substring(0, 490) : msg);
            fail.setRetryCount((record.getRetryCount() == null ? 0 : record.getRetryCount()) + 1);
            recordMapper.updatePushResult(fail);
            return false;
        }
    }
}