package com.ruoyi.mdm.maintenance.mapper;

import java.util.List;
import com.ruoyi.mdm.maintenance.domain.MdmAuditFlow;

/**
 * 审核流程配置 数据层
 *
 * @author ruoyi
 */
public interface MdmAuditFlowMapper
{
    /**
     * 按对象查询审核流程
     *
     * @param objectId 对象ID
     * @return 审核流程
     */
    public MdmAuditFlow selectFlowByObjectId(Long objectId);

    /**
     * 查询审核流程列表
     *
     * @param mdmAuditFlow 审核流程
     * @return 审核流程集合
     */
    public List<MdmAuditFlow> selectFlowList(MdmAuditFlow mdmAuditFlow);

    /**
     * 新增审核流程
     *
     * @param mdmAuditFlow 审核流程
     * @return 结果
     */
    public int insertFlow(MdmAuditFlow mdmAuditFlow);

    /**
     * 修改审核流程
     *
     * @param mdmAuditFlow 审核流程
     * @return 结果
     */
    public int updateFlow(MdmAuditFlow mdmAuditFlow);
}
