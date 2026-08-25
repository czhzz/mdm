package com.ruoyi.mdm.integration.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mdm.integration.domain.MdmApp;
import com.ruoyi.mdm.integration.domain.MdmDistributeApi;
import com.ruoyi.mdm.integration.domain.MdmDistributeLog;

/**
 * 集成管理 服务层（应用 / 分发 / 日志；接收与查询第 3 周扩展）
 *
 * @author ruoyi
 */
public interface IIntegrationService
{
    // ===== 应用凭证 =====

    public List<MdmApp> listApp(MdmApp mdmApp);

    public MdmApp getApp(Long appId);

    /** 按 appid 查询（对外接口鉴权用） */
    public MdmApp getAppByAppid(String appid);

    /** 新增并生成 appid/secret，返回含凭据的应用（凭据仅此一次可见） */
    public MdmApp addApp(MdmApp mdmApp);

    public int editApp(MdmApp mdmApp);

    public int deleteApps(Long[] appIds);

    /** 重置密钥，返回新 secret */
    public String resetSecret(Long appId);

    // ===== 分发配置 =====

    public List<MdmDistributeApi> listDist(MdmDistributeApi mdmDistributeApi);

    public MdmDistributeApi getDist(Long distId);

    public int addDist(MdmDistributeApi mdmDistributeApi);

    public int editDist(MdmDistributeApi mdmDistributeApi);

    public int deleteDists(Long[] distIds);

    // ===== 分发日志 =====

    public List<MdmDistributeLog> listRecord(MdmDistributeLog mdmDistributeLog);

    /** 失败重推 */
    public int retryRecord(Long recordId);

    /** 订阅方确认回执 */
    public int confirmRecord(Long recordId);

    // ===== 内嵌分发器 =====

    /** 数据变更后触发分发（新增/修改/审核落地均调用） */
    public void triggerPush(String objectCode, Long dataId, String actionType, Map<String, Object> data);

    // ===== 分发监控（1.1.0） =====

    /** 获取 MQ 分发监控数据（队列积压/成功率/延迟） */
    public Map<String, Object> getMqMonitorData();
}
