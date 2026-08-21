package com.ruoyi.mdm.maintenance.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mdm.maintenance.domain.MdmAuditTask;

/**
 * 主数据审核 服务层
 *
 * @author ruoyi
 */
public interface IMdmAuditService
{
    /**
     * 查询审核任务列表
     *
     * @param mdmAuditTask 审核任务
     * @return 审核任务集合
     */
    public List<MdmAuditTask> listTask(MdmAuditTask mdmAuditTask);

    /**
     * 提交审核（保存变更前后快照）
     *
     * @param objectCode 对象编码
     * @param dataId 数据ID（新增为0）
     * @param actionType 操作类型（INSERT/UPDATE）
     * @param afterData 变更后数据
     * @return 结果
     */
    public int submitAudit(String objectCode, Long dataId, String actionType, Map<String, Object> afterData);

    /**
     * 审核通过（将变更落地到业务表）
     *
     * @param taskId 任务ID
     * @return 结果
     */
    public int approve(Long taskId);

    /**
     * 审核驳回
     *
     * @param taskId 任务ID
     * @param reason 驳回原因
     * @return 结果
     */
    public int reject(Long taskId, String reason);
}
