package com.ruoyi.mdm.maintenance.mapper;

import java.util.List;
import com.ruoyi.mdm.maintenance.domain.MdmAuditTask;

/**
 * 主数据审核任务 数据层
 *
 * @author ruoyi
 */
public interface MdmAuditTaskMapper
{
    /**
     * 查询审核任务
     *
     * @param taskId 任务ID
     * @return 审核任务
     */
    public MdmAuditTask selectTaskById(Long taskId);

    /**
     * 查询审核任务列表
     *
     * @param mdmAuditTask 审核任务
     * @return 审核任务集合
     */
    public List<MdmAuditTask> selectTaskList(MdmAuditTask mdmAuditTask);

    /**
     * 新增审核任务
     *
     * @param mdmAuditTask 审核任务
     * @return 结果
     */
    public int insertTask(MdmAuditTask mdmAuditTask);

    /**
     * 更新审核结果（通过/驳回）
     *
     * @param mdmAuditTask 审核任务
     * @return 结果
     */
    public int updateTaskResult(MdmAuditTask mdmAuditTask);
}
