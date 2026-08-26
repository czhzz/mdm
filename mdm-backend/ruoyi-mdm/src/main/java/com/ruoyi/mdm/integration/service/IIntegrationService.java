package com.ruoyi.mdm.integration.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mdm.integration.domain.MdmApp;
import com.ruoyi.mdm.integration.domain.MdmDistributeApi;
import com.ruoyi.mdm.integration.domain.MdmDistributeLog;
import com.ruoyi.mdm.integration.domain.MdmQueryApi;
import com.ruoyi.mdm.integration.domain.MdmQueryLog;
import com.ruoyi.mdm.integration.domain.MdmReceiveApi;
import com.ruoyi.mdm.integration.domain.MdmReceiveLog;

/**
 * 集成管理 服务层（应用 / 接收 / 查询 / 分发 / 日志）
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

    // ===== 接收接口配置 =====

    public List<MdmReceiveApi> listReceiveApi(MdmReceiveApi query);

    public MdmReceiveApi getReceiveApi(Long id);

    public int addReceiveApi(MdmReceiveApi mdmReceiveApi);

    public int editReceiveApi(MdmReceiveApi mdmReceiveApi);

    public int deleteReceiveApis(Long[] ids);

    // ===== 查询接口配置 =====

    public List<MdmQueryApi> listQueryApi(MdmQueryApi query);

    public MdmQueryApi getQueryApi(Long id);

    public int addQueryApi(MdmQueryApi mdmQueryApi);

    public int editQueryApi(MdmQueryApi mdmQueryApi);

    public int deleteQueryApis(Long[] ids);

    // ===== 集成日志 =====

    public List<MdmReceiveLog> listReceiveLog(MdmReceiveLog query);

    public List<MdmQueryLog> listQueryLog(MdmQueryLog query);

    /** 手动清理日志（type: receive|query|distribute，删除截止时间前） */
    public int cleanLog(String type, String beforeTime);

    // ===== 对外接收 / 查询（/open/integration） =====

    /**
     * 对外接收数据（appid/secret 鉴权已由 ApiAuthFilter 完成）
     *
     * @param apiCode 接口编码
     * @param dataCode 外部业务键（幂等键，映射对象唯一属性）
     * @param data 数据（键为属性编码）
     * @param appCode 应用编码（appid）
     * @param ip 来源 IP
     * @return 处理结果（含审核提示）
     */
    public Map<String, Object> receive(String apiCode, String dataCode, Map<String, Object> data,
            String appCode, String ip);

    /**
     * 对外查询数据
     *
     * @param apiCode 接口编码
     * @param filters 条件（键为属性编码）
     * @return 分页结果（total/rows）
     */
    public Map<String, Object> queryOpen(String apiCode, Map<String, Object> filters, int pageNum,
            int pageSize, String appCode, String ip);

    // ===== 内嵌分发器 =====

    /** 数据变更后触发分发（新增/修改/审核落地均调用） */
    public void triggerPush(String objectCode, Long dataId, String actionType, Map<String, Object> data);

    // ===== 分发监控（1.1.0） =====

    /** 获取 MQ 分发监控数据（队列积压/成功率/延迟） */
    public Map<String, Object> getMqMonitorData();
}
