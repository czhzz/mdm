package com.ruoyi.web.controller.mdm;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mdm.maintenance.domain.MdmAuditTask;
import com.ruoyi.mdm.maintenance.service.IMdmAuditService;

/**
 * 主数据审核 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/audit")
public class MdmAuditController extends BaseController
{
    @Autowired
    private IMdmAuditService auditService;

    /**
     * 查询审核任务列表（待办）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:list')")
    @GetMapping("/task/list")
    public TableDataInfo list(MdmAuditTask mdmAuditTask)
    {
        startPage();
        List<MdmAuditTask> list = auditService.listTask(mdmAuditTask);
        return getDataTable(list);
    }

    /**
     * 提交审核（新增/修改）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:add')")
    @Log(title = "提交审核", businessType = BusinessType.INSERT)
    @PostMapping("/task/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> body)
    {
        String objectCode = String.valueOf(body.getOrDefault("objectCode", ""));
        Long dataId = body.get("dataId") == null ? 0L : Long.valueOf(String.valueOf(body.get("dataId")));
        String actionType = String.valueOf(body.getOrDefault("actionType", ""));
        Map<String, Object> afterData = (Map<String, Object>) body.get("afterData");
        return toAjax(auditService.submitAudit(objectCode, dataId, actionType, afterData));
    }

    /**
     * 审核通过
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:edit')")
    @Log(title = "审核通过", businessType = BusinessType.UPDATE)
    @PutMapping("/task/approve/{taskId}")
    public AjaxResult approve(@PathVariable Long taskId)
    {
        return toAjax(auditService.approve(taskId));
    }

    /**
     * 审核驳回
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:edit')")
    @Log(title = "审核驳回", businessType = BusinessType.UPDATE)
    @PutMapping("/task/reject/{taskId}")
    public AjaxResult reject(@PathVariable Long taskId, @RequestBody Map<String, Object> body)
    {
        String reason = String.valueOf(body.getOrDefault("reason", ""));
        return toAjax(auditService.reject(taskId, reason));
    }
}
