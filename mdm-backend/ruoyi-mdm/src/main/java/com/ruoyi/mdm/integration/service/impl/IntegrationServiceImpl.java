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
import com.ruoyi.mdm.integration.domain.MdmQueryApi;
import com.ruoyi.mdm.integration.domain.MdmQueryLog;
import com.ruoyi.mdm.integration.domain.MdmReceiveApi;
import com.ruoyi.mdm.integration.domain.MdmReceiveLog;
import com.ruoyi.mdm.integration.mapper.MdmAppMapper;
import com.ruoyi.mdm.integration.mapper.MdmDistributeApiMapper;
import com.ruoyi.mdm.integration.mapper.MdmDistributeLogMapper;
import com.ruoyi.mdm.integration.mapper.MdmQueryApiMapper;
import com.ruoyi.mdm.integration.mapper.MdmQueryLogMapper;
import com.ruoyi.mdm.integration.mapper.MdmReceiveApiMapper;
import com.ruoyi.mdm.integration.mapper.MdmReceiveLogMapper;
import com.ruoyi.mdm.integration.service.IIntegrationService;
import com.ruoyi.mdm.audit.service.IMdmAuditFlowableService;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmAttributeMapper;
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
    private MdmAttributeMapper attributeMapper;

    @Autowired
    private MdmReceiveApiMapper receiveApiMapper;

    @Autowired
    private MdmQueryApiMapper queryApiMapper;

    @Autowired
    private MdmReceiveLogMapper receiveLogMapper;

    @Autowired
    private MdmQueryLogMapper queryLogMapper;

    @Autowired
    private IMdmDataService dataService;

    @Autowired
    private IMdmAuditFlowableService auditFlowableService;

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

    // ===== 接收接口配置 =====

    @Override
    public List<MdmReceiveApi> listReceiveApi(MdmReceiveApi query)
    {
        return receiveApiMapper.selectList(query);
    }

    @Override
    public MdmReceiveApi getReceiveApi(Long id)
    {
        return receiveApiMapper.selectById(id);
    }

    @Override
    public int addReceiveApi(MdmReceiveApi mdmReceiveApi)
    {
        if (StringUtils.isEmpty(mdmReceiveApi.getApiCode()) || StringUtils.isEmpty(mdmReceiveApi.getObjectCode()))
        {
            throw new ServiceException("接口编码与目标对象不能为空");
        }
        if (receiveApiMapper.selectByApiCode(mdmReceiveApi.getApiCode()) != null)
        {
            throw new ServiceException("接口编码已存在：" + mdmReceiveApi.getApiCode());
        }
        mdmReceiveApi.setStatus(StringUtils.isEmpty(mdmReceiveApi.getStatus()) ? "0" : mdmReceiveApi.getStatus());
        mdmReceiveApi.setCreateBy(SecurityUtils.getUsername());
        return receiveApiMapper.insert(mdmReceiveApi);
    }

    @Override
    public int editReceiveApi(MdmReceiveApi mdmReceiveApi)
    {
        mdmReceiveApi.setUpdateBy(SecurityUtils.getUsername());
        return receiveApiMapper.update(mdmReceiveApi);
    }

    @Override
    public int deleteReceiveApis(Long[] ids)
    {
        return receiveApiMapper.deleteByIds(ids);
    }

    // ===== 查询接口配置 =====

    @Override
    public List<MdmQueryApi> listQueryApi(MdmQueryApi query)
    {
        return queryApiMapper.selectList(query);
    }

    @Override
    public MdmQueryApi getQueryApi(Long id)
    {
        return queryApiMapper.selectById(id);
    }

    @Override
    public int addQueryApi(MdmQueryApi mdmQueryApi)
    {
        if (StringUtils.isEmpty(mdmQueryApi.getApiCode()) || StringUtils.isEmpty(mdmQueryApi.getObjectCode()))
        {
            throw new ServiceException("接口编码与目标对象不能为空");
        }
        if (queryApiMapper.selectByApiCode(mdmQueryApi.getApiCode()) != null)
        {
            throw new ServiceException("接口编码已存在：" + mdmQueryApi.getApiCode());
        }
        mdmQueryApi.setStatus(StringUtils.isEmpty(mdmQueryApi.getStatus()) ? "0" : mdmQueryApi.getStatus());
        mdmQueryApi.setCreateBy(SecurityUtils.getUsername());
        return queryApiMapper.insert(mdmQueryApi);
    }

    @Override
    public int editQueryApi(MdmQueryApi mdmQueryApi)
    {
        mdmQueryApi.setUpdateBy(SecurityUtils.getUsername());
        return queryApiMapper.update(mdmQueryApi);
    }

    @Override
    public int deleteQueryApis(Long[] ids)
    {
        return queryApiMapper.deleteByIds(ids);
    }

    // ===== 集成日志 =====

    @Override
    public List<MdmReceiveLog> listReceiveLog(MdmReceiveLog query)
    {
        return receiveLogMapper.selectList(query);
    }

    @Override
    public List<MdmQueryLog> listQueryLog(MdmQueryLog query)
    {
        return queryLogMapper.selectList(query);
    }

    @Override
    public int cleanLog(String type, String beforeTime)
    {
        if (StringUtils.isEmpty(beforeTime))
        {
            throw new ServiceException("截止时间不能为空");
        }
        switch (type)
        {
            case "receive":
                return receiveLogMapper.deleteBeforeTime(beforeTime);
            case "query":
                return queryLogMapper.deleteBeforeTime(beforeTime);
            case "distribute":
                return recordMapper.deleteBeforeTime(beforeTime);
            default:
                throw new ServiceException("不支持的日志类型：" + type + "（receive|query|distribute）");
        }
    }

    // ===== 对外接收 / 查询 =====

    @Override
    public Map<String, Object> receive(String apiCode, String dataCode, Map<String, Object> data,
            String appCode, String ip)
    {
        MdmReceiveApi api = receiveApiMapper.selectByApiCode(apiCode);
        if (api == null)
        {
            throw new ServiceException("接收接口不存在：" + apiCode);
        }
        if (!"0".equals(api.getStatus()))
        {
            throw new ServiceException("接收接口已停用：" + apiCode);
        }
        String objectCode = api.getObjectCode();
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (object == null || !"1".equals(object.getStatus()))
        {
            throw new ServiceException("目标数据对象未发布：" + objectCode);
        }
        long start = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        try
        {
            // 幂等键：dataCode 映射对象唯一属性（primaryFlag 优先，其次 uniqueFlag）
            String uniqueCol = findUniqueColumn(object.getObjectId());
            Long existId = null;
            if (uniqueCol != null && StringUtils.isNotEmpty(dataCode))
            {
                data.put(uniqueCol, dataCode);
                List<Map<String, Object>> rows = dataService.selectDataList(objectCode,
                        new LinkedHashMap<String, Object>()
                        {
                            private static final long serialVersionUID = 1L;
                            {
                                put(uniqueCol, dataCode);
                            }
                        }, 1, 1);
                if (!rows.isEmpty())
                {
                    existId = Long.valueOf(String.valueOf(rows.get(0).get("id")));
                }
            }
            boolean update = existId != null;
            if (update)
            {
                dataService.updateData(objectCode, existId, data);
            }
            else
            {
                dataService.insertDataWithSource(objectCode, data, "API:" + appCode);
            }
            // 柔性落库：绑定审核流程则提交审核，否则直接生效
            boolean auditEnabled = StringUtils.isNotEmpty(object.getAuditProcessKey());
            if (auditEnabled)
            {
                auditFlowableService.submitAudit(objectCode, update ? existId : 0L, update ? "UPDATE" : "INSERT", data);
            }
            else if (!update)
            {
                // 直接生效（插入成功后按唯一键回查 id 置生效）
                if (uniqueCol != null)
                {
                    List<Map<String, Object>> rows = dataService.selectDataList(objectCode,
                            new LinkedHashMap<String, Object>()
                            {
                                private static final long serialVersionUID = 1L;
                                {
                                    put(uniqueCol, dataCode);
                                }
                            }, 1, 1);
                    if (!rows.isEmpty())
                    {
                        dataService.updateDataStatus(objectCode, Long.valueOf(String.valueOf(rows.get(0).get("id"))), "1");
                    }
                }
            }
            result.put("success", true);
            result.put("message", update ? "更新成功" : (auditEnabled ? "接收成功，已提交审核" : "接收成功"));
            writeReceiveLog(api, dataCode, appCode, ip, "0", null, start);
            return result;
        }
        catch (Exception e)
        {
            writeReceiveLog(api, dataCode, appCode, ip, "1", e.getMessage(), start);
            throw e;
        }
    }

    @Override
    public Map<String, Object> queryOpen(String apiCode, Map<String, Object> filters, int pageNum,
            int pageSize, String appCode, String ip)
    {
        MdmQueryApi api = queryApiMapper.selectByApiCode(apiCode);
        if (api == null)
        {
            throw new ServiceException("查询接口不存在：" + apiCode);
        }
        if (!"0".equals(api.getStatus()))
        {
            throw new ServiceException("查询接口已停用：" + apiCode);
        }
        long start = System.currentTimeMillis();
        try
        {
            List<Map<String, Object>> rows = dataService.selectDataList(api.getObjectCode(), filters, pageNum, pageSize);
            long total = dataService.countData(api.getObjectCode(), filters);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total", total);
            result.put("rows", rows);
            MdmQueryLog log = new MdmQueryLog();
            log.setAppCode(appCode);
            log.setObjectCode(api.getObjectCode());
            log.setSuccess("0");
            log.setCostMs((int) (System.currentTimeMillis() - start));
            log.setIp(ip);
            log.setResultCount(rows.size());
            log.setRequestSummary(truncate(filters == null ? "" : filters.toString(), 500));
            queryLogMapper.insert(log);
            return result;
        }
        catch (Exception e)
        {
            MdmQueryLog log = new MdmQueryLog();
            log.setAppCode(appCode);
            log.setObjectCode(api.getObjectCode());
            log.setSuccess("1");
            log.setErrorMsg(truncate(e.getMessage(), 2000));
            log.setCostMs((int) (System.currentTimeMillis() - start));
            log.setIp(ip);
            log.setResultCount(0);
            queryLogMapper.insert(log);
            throw e;
        }
    }

    /** 幂等键列：主属性优先，其次首个唯一属性；无唯一属性返回 null（无法幂等，仅追加） */
    private String findUniqueColumn(Long objectId)
    {
        MdmAttribute query = new MdmAttribute();
        query.setObjectId(objectId);
        List<MdmAttribute> attrs = attributeMapper.selectAttributeList(query);
        for (MdmAttribute a : attrs)
        {
            if ("Y".equals(a.getPrimaryFlag()))
            {
                return a.getAttrCode();
            }
        }
        for (MdmAttribute a : attrs)
        {
            if ("Y".equals(a.getUniqueFlag()))
            {
                return a.getAttrCode();
            }
        }
        return null;
    }

    private void writeReceiveLog(MdmReceiveApi api, String dataCode, String appCode, String ip,
            String success, String error, long start)
    {
        MdmReceiveLog log = new MdmReceiveLog();
        log.setAppCode(appCode);
        log.setObjectCode(api.getObjectCode());
        log.setBusinessCode(dataCode);
        log.setSuccess(success);
        log.setErrorMsg(truncate(error, 2000));
        log.setCostMs((int) (System.currentTimeMillis() - start));
        log.setIp(ip);
        receiveLogMapper.insert(log);
    }

    private String truncate(String s, int max)
    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
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
            record.setCreateBy(currentUsername());
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

    /** 当前操作人（匿名场景——对外接口——无登录用户，回退 "API"） */
    private String currentUsername()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return "API";
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
