package com.ruoyi.mdm.audit.service;

import java.util.List;
import java.util.Map;

/**
 * Flowable 审核流程Service接口
 *
 * @author ruoyi
 */
public interface IMdmAuditFlowableService
{
    // ===== 流程定义管理 =====

    /** 查询已部署流程定义列表 */
    List<Map<String, Object>> listProcessDefinitions();

    /** 部署流程（BPMN XML 字符串） */
    String deployProcess(String name, String bpmnXml);

    /** 删除流程定义 */
    void deleteProcessDefinition(String deploymentId);

    /** 挂起流程定义 */
    void suspendProcessDefinition(String processDefinitionId);

    /** 激活流程定义 */
    void activateProcessDefinition(String processDefinitionId);

    // ===== 审核任务 =====

    /** 提交审核——启动流程实例 */
    void submitAudit(String objectCode, Long dataId, String actionType, Map<String, Object> data);

    /** 查询待办任务 */
    List<Map<String, Object>> listTodoTasks(String assignee);

    /** 查询已办任务 */
    List<Map<String, Object>> listDoneTasks(String assignee);

    /** 审批通过 */
    void approve(String taskId, String comment);

    /** 审批驳回 */
    void reject(String taskId, String comment);

    /** 退回（退回上一步） */
    void back(String taskId, String comment);

    /** 转办 */
    void delegateTask(String taskId, String targetUser);

    /** 加签 */
    void addSign(String taskId, String targetUser);

    /** 获取任务详情（含流程变量） */
    Map<String, Object> getTaskDetail(String taskId);

    /** 获取流程进度（当前节点高亮） */
    List<Map<String, Object>> getProcessProgress(String processInstanceId);
}