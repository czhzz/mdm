package com.ruoyi.mdm.audit.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mdm.audit.service.IMdmAuditFlowableService;

/**
 * Flowable 审核流程Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/audit/flowable")
public class MdmAuditFlowableController extends BaseController
{
    @Autowired
    private IMdmAuditFlowableService auditFlowableService;

    // ===== 流程定义 =====

    @GetMapping("/definitions")
    public AjaxResult listDefinitions()
    {
        return success(auditFlowableService.listProcessDefinitions());
    }

    @PostMapping("/deploy")
    public AjaxResult deploy(@RequestBody Map<String, String> body)
    {
        String name = body.get("name");
        String bpmnXml = body.get("bpmnXml");
        String key = auditFlowableService.deployProcess(name, bpmnXml);
        return success(key);
    }

    @DeleteMapping("/definition/{deploymentId}")
    public AjaxResult deleteDefinition(@PathVariable String deploymentId)
    {
        auditFlowableService.deleteProcessDefinition(deploymentId);
        return success();
    }

    @PutMapping("/definition/{definitionId}/suspend")
    public AjaxResult suspend(@PathVariable String definitionId)
    {
        auditFlowableService.suspendProcessDefinition(definitionId);
        return success();
    }

    @PutMapping("/definition/{definitionId}/activate")
    public AjaxResult activate(@PathVariable String definitionId)
    {
        auditFlowableService.activateProcessDefinition(definitionId);
        return success();
    }

    // ===== 审核任务 =====

    @GetMapping("/todo")
    public TableDataInfo todo(@RequestParam(required = false) String assignee)
    {
        if (assignee == null)
        {
            assignee = getUsername();
        }
        startPage();
        return getDataTable(auditFlowableService.listTodoTasks(assignee));
    }

    @GetMapping("/done")
    public TableDataInfo done(@RequestParam(required = false) String assignee)
    {
        if (assignee == null)
        {
            assignee = getUsername();
        }
        startPage();
        return getDataTable(auditFlowableService.listDoneTasks(assignee));
    }

    @GetMapping("/task/{taskId}")
    public AjaxResult taskDetail(@PathVariable String taskId)
    {
        return success(auditFlowableService.getTaskDetail(taskId));
    }

    @PutMapping("/task/{taskId}/approve")
    public AjaxResult approve(@PathVariable String taskId, @RequestBody Map<String, String> body)
    {
        auditFlowableService.approve(taskId, body.getOrDefault("comment", ""));
        return success("审批通过");
    }

    @PutMapping("/task/{taskId}/reject")
    public AjaxResult reject(@PathVariable String taskId, @RequestBody Map<String, String> body)
    {
        auditFlowableService.reject(taskId, body.getOrDefault("comment", ""));
        return success("已驳回");
    }

    @PutMapping("/task/{taskId}/back")
    public AjaxResult back(@PathVariable String taskId, @RequestBody Map<String, String> body)
    {
        auditFlowableService.back(taskId, body.getOrDefault("comment", ""));
        return success("已退回");
    }

    @PutMapping("/task/{taskId}/delegate")
    public AjaxResult delegate(@PathVariable String taskId, @RequestBody Map<String, String> body)
    {
        auditFlowableService.delegateTask(taskId, body.get("targetUser"));
        return success("已转办");
    }

    @PutMapping("/task/{taskId}/add-sign")
    public AjaxResult addSign(@PathVariable String taskId, @RequestBody Map<String, String> body)
    {
        auditFlowableService.addSign(taskId, body.get("targetUser"));
        return success("已加签");
    }

    @GetMapping("/progress/{processInstanceId}")
    public AjaxResult progress(@PathVariable String processInstanceId)
    {
        return success(auditFlowableService.getProcessProgress(processInstanceId));
    }
}