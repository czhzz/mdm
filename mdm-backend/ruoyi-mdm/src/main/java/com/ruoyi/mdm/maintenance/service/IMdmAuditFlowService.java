package com.ruoyi.mdm.maintenance.service;

import com.ruoyi.mdm.maintenance.domain.MdmAuditFlow;

/**
 * 审核流程配置 服务层
 *
 * @author ruoyi
 */
public interface IMdmAuditFlowService
{
    /**
     * 按对象查询审核流程
     *
     * @param objectId 对象ID
     * @return 审核流程（无配置时返回 null）
     */
    public MdmAuditFlow selectFlowByObjectId(Long objectId);

    /**
     * 保存审核流程（存在则更新，不存在则新增）
     *
     * @param mdmAuditFlow 审核流程
     * @return 结果
     */
    public int saveFlow(MdmAuditFlow mdmAuditFlow);
}
