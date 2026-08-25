package com.ruoyi.mdm.integration.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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
import com.ruoyi.mdm.integration.config.RabbitMQConfig;
import com.ruoyi.mdm.integration.domain.MdmApp;
import com.ruoyi.mdm.integration.domain.MdmDistributeApi;
import com.ruoyi.mdm.integration.domain.MdmDistributeLog;
import com.ruoyi.mdm.integration.mapper.MdmAppMapper;
import com.ruoyi.mdm.integration.mapper.MdmDistributeApiMapper;
import com.ruoyi.mdm.integration.mapper.MdmDistributeLogMapper;
import com.ruoyi.mdm.integration.service.IIntegrationService;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

/**
 * 集成管理 服务层实现（自 distribution 迁入，表随 1.2.0 更名 mdm_distribute_api/log）
 *
 * <p>内嵌分发器：变更即推默认异步线程推送（复用若依 AsyncManager），结果写分发日志表；
 * 失败可通过记录重推。订阅方不可用只影响该次推送，不回滚主流程。
 *
 * @author ruoyi
 */
@Service
public class IntegrationServiceImpl implements IIntegrationService
{
    @Autowired
    private MdmAppMapper appMapper;

    @Autowired
    private MdmDistributeApiMapper distMapper;

    @Autowired
    private MdmDistributeLogMapper recordMapper;

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    /** 异步推送线程池（daemon，订阅方不可用不阻塞主流程） */
    // ponytail: 固定小线程池，订阅方规模扩大或推送量大时按设计决策五引入 RabbitMQ 替换
    private static final ExecutorService PUSH_EXECUTOR = Executors.newFixedThreadPool(2, r ->
    {
        Thread t = new Thread(r, "mdm-integration-push");
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
        // 新增应用默认启用，便于立即接入
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
    public List<MdmDistributeApi> listDist(MdmDistributeApi mdmDistributeApi)
    {
        return distMapper.selectDistList(mdmDistributeApi);
    }

    @Override
    public MdmDistributeApi getDist(Long distId)
    {
        return distMapper.selectDistById(distId);
    }

    @Override
    public int addDist(MdmDistributeApi mdmDistributeApi)
    {
        mdmDistributeApi.setTriggerType(StringUtils.isEmpty(mdmDistributeApi.getTriggerType()) ? "IMMEDIATE" : mdmDistributeApi.getTriggerType());
        mdmDistributeApi.setEnabled(StringUtils.isEmpty(mdmDistributeApi.getEnabled()) ? "1" : mdmDistributeApi.getEnabled());
        mdmDistributeApi.setCreateBy(SecurityUtils.getUsername());
        return distMapper.insertDist(mdmDistributeApi);
    }

    @Override
    public int editDist(MdmDistributeApi mdmDistributeApi)
    {
        mdmDistributeApi.setUpdateBy(SecurityUtils.getUsername());
        return distMapper.updateDist(mdmDistributeApi);
    }

    @Override
    public int deleteDists(Long[] distIds)
    {
        return distMapper.deleteDistByIds(distIds);
    }

    // ===== 分发日志 =====

    @Override
    public List<MdmDistributeLog> listRecord(MdmDistributeLog mdmDistributeLog)
    {
        return recordMapper.selectRecordList(mdmDistributeLog);
    }

    @Override
    public int retryRecord(Long recordId)
    {
        MdmDistributeLog record = recordMapper.selectRecordById(recordId);
        if (record == null)
        {
            throw new ServiceException("分发记录不存在");
        }
        // 重推优先使用当前配置回调地址（如订阅方已修好），而非记录里的历史死地址
        MdmObject object = objectMapper.checkObjectCodeUnique(record.getObjectCode());
        if (object != null)
        {
            for (MdmDistributeApi cfg : distMapper.selectEnabledListByObjectId(object.getObjectId()))
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
        MdmDistributeLog confirm = new MdmDistributeLog();
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
        List<MdmDistributeApi> configs = distMapper.selectEnabledListByObjectId(object.getObjectId());
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
        for (MdmDistributeApi config : configs)
        {
            MdmDistributeLog record = new MdmDistributeLog();
            record.setAppId(config.getAppId());
            // 回填应用编码（统一筛选维度）
            MdmApp app = appMapper.selectAppById(config.getAppId());
            record.setAppCode(app == null ? null : app.getAppid());
            record.setObjectCode(objectCode);
            record.setDataId(dataId == null ? 0L : dataId);
            record.setActionType(actionType);
            record.setEndpointUrl(config.getEndpointUrl());
            record.setPayload(payload);
            record.setStatus("0");
            record.setCreateBy(SecurityUtils.getUsername());
            recordMapper.insertRecord(record);
            // 1.1.0：按通道分流——HTTP 走线程池推送，MQ 走 RabbitMQ 消息
            if ("MQ".equalsIgnoreCase(config.getChannel()))
            {
                sendMq(record, config);
            }
            else
            {
                // 异步推送，订阅方不可用不阻塞主流程
                PUSH_EXECUTOR.submit(() -> send(record, false));
            }
        }
    }

    /**
     * MQ 通道推送：消息发送到 Topic Exchange，路由到订阅方队列
     */
    private void sendMq(MdmDistributeLog record, MdmDistributeApi config)
    {
        try
        {
            String queueName = StringUtils.isNotEmpty(config.getQueueName())
                    ? config.getQueueName()
                    : RabbitMQConfig.ROUTING_KEY_PREFIX + record.getObjectCode();
            // 动态声明订阅方队列（幂等，已存在则跳过）
            Queue queue = QueueBuilder.durable(queueName)
                    .withArguments(RabbitMQConfig.queueArgs(queueName + ".dlq")).build();
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(new org.springframework.amqp.core.Binding(
                    queueName, org.springframework.amqp.core.Binding.DestinationType.QUEUE,
                    RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_PREFIX + record.getObjectCode(), null));
            // 发送消息（JSON 转换器在 RabbitTemplate 已配置）
            String routingKey = RabbitMQConfig.ROUTING_KEY_PREFIX + record.getObjectCode();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, record.getPayload());
            // 发送成功（发布确认）
            MdmDistributeLog ok = new MdmDistributeLog();
            ok.setRecordId(record.getRecordId());
            ok.setStatus("1");
            ok.setSuccessTime(DateUtils.dateTimeNow());
            recordMapper.updatePushResult(ok);
        }
        catch (Exception e)
        {
            MdmDistributeLog fail = new MdmDistributeLog();
            fail.setRecordId(record.getRecordId());
            fail.setStatus("2");
            String msg = StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
            fail.setErrorMsg(msg.length() > 490 ? msg.substring(0, 490) : msg);
            fail.setRetryCount((record.getRetryCount() == null ? 0 : record.getRetryCount()) + 1);
            recordMapper.updatePushResult(fail);
        }
    }

    // ===== 分发监控（1.1.0） =====

    @Override
    public Map<String, Object> getMqMonitorData()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        // 死信队列积压
        Object dlxCount = 0;
        try
        {
            org.springframework.amqp.core.AmqpAdmin admin = rabbitAdmin;
            if (admin != null)
            {
                Properties props = admin.getQueueProperties(RabbitMQConfig.DLX_QUEUE);
                if (props != null)
                {
                    dlxCount = props.getOrDefault("QUEUE_MESSAGE_COUNT", 0);
                }
            }
        }
        catch (Exception e)
        {
            dlxCount = -1; // MQ 不可用
        }
        result.put("dlxCount", dlxCount);
        // 统计分发成功率（近 100 条记录）
        List<MdmDistributeLog> records = recordMapper.selectRecentRecords(100);
        long total = records.size();
        long success = records.stream().filter(r -> "1".equals(r.getStatus())).count();
        result.put("successRate", total == 0 ? 100.0 : Math.round(success * 1000.0 / total) / 10.0);
        result.put("totalRecords", total);
        // 各通道分布
        long mqCount = records.stream().filter(r -> "MQ".equalsIgnoreCase(r.getEndpointUrl())).count();
        result.put("mqCount", mqCount);
        return result;
    }

    private boolean send(MdmDistributeLog record, boolean sync)
    {
        MdmDistributeLog sending = new MdmDistributeLog();
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
            MdmDistributeLog ok = new MdmDistributeLog();
            ok.setRecordId(record.getRecordId());
            ok.setStatus("1");
            ok.setSuccessTime(DateUtils.dateTimeNow());
            recordMapper.updatePushResult(ok);
            return true;
        }
        catch (Exception e)
        {
            MdmDistributeLog fail = new MdmDistributeLog();
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
