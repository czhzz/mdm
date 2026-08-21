package com.ruoyi.mdm.distribution.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mdm.distribution.domain.MdmApp;
import com.ruoyi.mdm.distribution.domain.MdmDistribution;
import com.ruoyi.mdm.distribution.domain.MdmDistributionRecord;

/**
 * 主数据分发与集成 服务层
 *
 * @author ruoyi
 */
public interface IDistributionService
{
    // ===== 应用凭证 =====

    public List<MdmApp> listApp(MdmApp mdmApp);

    public MdmApp getApp(Long appId);

    /** 按 appid 查询（对外接口鉴权用） */
    public MdmApp getAppByAppid(String appid);

    /** 新增并生成 appid/secret */
    public int addApp(MdmApp mdmApp);

    public int editApp(MdmApp mdmApp);

    public int deleteApps(Long[] appIds);

    /** 重置密钥，返回新 secret */
    public String resetSecret(Long appId);

    // ===== 分发配置 =====

    public List<MdmDistribution> listDist(MdmDistribution mdmDistribution);

    public MdmDistribution getDist(Long distId);

    public int addDist(MdmDistribution mdmDistribution);

    public int editDist(MdmDistribution mdmDistribution);

    public int deleteDists(Long[] distIds);

    // ===== 分发记录 =====

    public List<MdmDistributionRecord> listRecord(MdmDistributionRecord mdmDistributionRecord);

    /** 失败重推 */
    public int retryRecord(Long recordId);

    /** 订阅方确认回执 */
    public int confirmRecord(Long recordId);

    // ===== 内嵌分发器 =====

    /** 数据变更后触发分发（新增/修改/审核落地均调用） */
    public void triggerPush(String objectCode, Long dataId, String actionType, Map<String, Object> data);
}