package com.ruoyi.mdm.audit.service.impl;

import java.util.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.audit.service.IMdmAuditFlowableService;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

/**
 * Flowable 审核流程Service实现
 *
 * @author ruoyi
 */
@Service
public class MdmAuditFlowableServiceImpl implements IMdmAuditFlowableService
{
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IMdmDataService dataService;

    @Autowired
    private MdmObjectMapper objectMapper;

    // ===== 流程定义管理 =====

    @Override
    public List<Map<String, Object>> listProcessDefinitions()
    {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .latestVersion().orderByProcessDefinitionKey().asc().list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessDefinition pd : list)
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", pd.getId());
            m.put("key", pd.getKey());
            m.put("name", pd.getName());
            m.put("version", pd.getVersion());
            m.put("deploymentId", pd.getDeploymentId());
            m.put("suspended", pd.isSuspended());
            m.put("resourceName", pd.getResourceName());
            result.add(m);
        }
        return result;
    }

    @Override
    @Transactional
    public String deployProcess(String name, String bpmnXml)
    {
        Deployment deployment = repositoryService.createDeployment()
                .name(name)
                .addString(name + ".bpmn20.xml", bpmnXml)
                .deploy();
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        return pd != null ? pd.getKey() : null;
    }

    @Override
    @Transactional
    public void deleteProcessDefinition(String deploymentId)
    {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    @Override
    @Transactional
    public void suspendProcessDefinition(String processDefinitionId)
    {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    @Override
    @Transactional
    public void activateProcessDefinition(String processDefinitionId)
    {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    // ===== 审核任务 =====

    @Override
    @Transactional
    public void submitAudit(String objectCode, Long dataId, String actionType, Map<String, Object> data)
    {
        MdmObject obj = objectMapper.checkObjectCodeUnique(objectCode);
        if (obj == null || StringUtils.isEmpty(obj.getAuditProcessKey()))
        {
            throw new ServiceException("该对象未配置审核流程");
        }
        String processKey = obj.getAuditProcessKey();
        // 检查流程定义是否存在
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey).latestVersion().count();
        if (count == 0)
        {
            throw new ServiceException("审核流程定义不存在：" + processKey);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("objectCode", objectCode);
        variables.put("dataId", dataId);
        variables.put("actionType", actionType);
        variables.put("data", data);
        // 1.2.0：匿名场景（集成接收接口提交审核）无登录用户，回退 "API"
        String submitter;
        try
        {
            submitter = SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            submitter = "API";
        }
        variables.put("submitter", submitter);
        // 启动流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey,
                objectCode + "_" + dataId + "_" + System.currentTimeMillis(), variables);
        // 记录流程实例 ID 到业务数据（后续可通过 objectCode + dataId 反查）
        runtimeService.setVariable(pi.getId(), "processInstanceId", pi.getId());
    }

    @Override
    public List<Map<String, Object>> listTodoTasks(String assignee)
    {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(assignee).orderByTaskCreateTime().desc().list();
        // 同时查询候选组任务
        List<Task> candidateTasks = taskService.createTaskQuery()
                .taskCandidateUser(assignee).orderByTaskCreateTime().desc().list();
        List<Map<String, Object>> result = new ArrayList<>();
        appendTasks(result, tasks);
        appendTasks(result, candidateTasks);
        result.sort((a, b) -> String.valueOf(b.getOrDefault("createTime", ""))
                .compareTo(String.valueOf(a.getOrDefault("createTime", ""))));
        return result;
    }

    @Override
    public List<Map<String, Object>> listDoneTasks(String assignee)
    {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee).finished().orderByHistoricTaskInstanceEndTime().desc().list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricTaskInstance t : tasks)
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("assignee", t.getAssignee());
            m.put("startTime", t.getStartTime());
            m.put("endTime", t.getEndTime());
            m.put("deleteReason", t.getDeleteReason());
            result.add(m);
        }
        return result;
    }

    @Override
    @Transactional
    public void approve(String taskId, String comment)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已处理");
        }
        if (StringUtils.isNotEmpty(comment))
        {
            taskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", true);
        taskService.complete(taskId, vars);
    }

    @Override
    @Transactional
    public void reject(String taskId, String comment)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已处理");
        }
        if (StringUtils.isNotEmpty(comment))
        {
            taskService.addComment(taskId, task.getProcessInstanceId(), "驳回: " + comment);
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", false);
        vars.put("rejectReason", comment);
        taskService.complete(taskId, vars);
        // 驳回后删除流程实例
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "驳回");
    }

    @Override
    @Transactional
    public void back(String taskId, String comment)
    {
        // 退回：完成当前任务并跳转到上一节点
        // Flowable 原生不支持退回，需手动处理
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已处理");
        }
        if (StringUtils.isNotEmpty(comment))
        {
            taskService.addComment(taskId, task.getProcessInstanceId(), "退回: " + comment);
        }
        // ponytail: 简化为驳回（退回=驳回），复杂退回路径需引入 Flowable 跳转 API
        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", false);
        vars.put("rejectReason", "退回: " + (comment != null ? comment : ""));
        taskService.complete(taskId, vars);
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "退回");
    }

    @Override
    @Transactional
    public void delegateTask(String taskId, String targetUser)
    {
        taskService.delegateTask(taskId, targetUser);
    }

    @Override
    @Transactional
    public void addSign(String taskId, String targetUser)
    {
        taskService.addCandidateUser(taskId, targetUser);
    }

    @Override
    public Map<String, Object> getTaskDetail(String taskId)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", task.getId());
        detail.put("name", task.getName());
        detail.put("processInstanceId", task.getProcessInstanceId());
        detail.put("assignee", task.getAssignee());
        detail.put("createTime", task.getCreateTime());
        // 流程变量
        Map<String, Object> vars = runtimeService.getVariables(task.getProcessInstanceId());
        detail.put("variables", vars);
        // 审批意见
        List<org.flowable.engine.task.Comment> comments = taskService.getTaskComments(taskId);
        List<Map<String, String>> commentList = new ArrayList<>();
        for (org.flowable.engine.task.Comment c : comments)
        {
            Map<String, String> cm = new LinkedHashMap<>();
            cm.put("userId", c.getUserId());
            cm.put("message", c.getFullMessage());
            cm.put("time", c.getTime() != null ? c.getTime().toString() : "");
            commentList.add(cm);
        }
        detail.put("comments", commentList);
        return detail;
    }

    @Override
    public List<Map<String, Object>> getProcessProgress(String processInstanceId)
    {
        List<Map<String, Object>> nodes = new ArrayList<>();
        // 历史节点
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).finished().orderByHistoricTaskInstanceEndTime().asc().list();
        for (HistoricTaskInstance t : historicTasks)
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("assignee", t.getAssignee());
            m.put("status", "completed");
            m.put("endTime", t.getEndTime());
            nodes.add(m);
        }
        // 当前节点
        List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        for (Task t : activeTasks)
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("assignee", t.getAssignee());
            m.put("status", "active");
            nodes.add(m);
        }
        return nodes;
    }

    private void appendTasks(List<Map<String, Object>> result, List<Task> tasks)
    {
        for (Task t : tasks)
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("assignee", t.getAssignee());
            m.put("createTime", t.getCreateTime());
            // 流程变量
            Map<String, Object> vars = runtimeService.getVariables(t.getProcessInstanceId());
            m.put("objectCode", vars.get("objectCode"));
            m.put("dataId", vars.get("dataId"));
            m.put("actionType", vars.get("actionType"));
            m.put("submitter", vars.get("submitter"));
            result.add(m);
        }
    }
}