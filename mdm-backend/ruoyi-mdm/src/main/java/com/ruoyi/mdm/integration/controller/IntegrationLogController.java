package com.ruoyi.mdm.integration.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mdm.integration.domain.MdmDistributeLog;
import com.ruoyi.mdm.integration.domain.MdmQueryLog;
import com.ruoyi.mdm.integration.domain.MdmReceiveLog;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成管理-集成日志 操作处理（接收/查询/分发三张日志表）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/integration/log")
public class IntegrationLogController extends BaseController
{
    @Autowired
    private IIntegrationService integrationService;

    @PreAuthorize("@ss.hasPermi('mdm:integration:log:list')")
    @GetMapping("/receive/list")
    public TableDataInfo listReceive(MdmReceiveLog mdmReceiveLog)
    {
        startPage();
        List<MdmReceiveLog> list = integrationService.listReceiveLog(mdmReceiveLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:log:list')")
    @GetMapping("/query/list")
    public TableDataInfo listQuery(MdmQueryLog mdmQueryLog)
    {
        startPage();
        List<MdmQueryLog> list = integrationService.listQueryLog(mdmQueryLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:log:list')")
    @GetMapping("/distribute/list")
    public TableDataInfo listDistribute(MdmDistributeLog mdmDistributeLog)
    {
        startPage();
        List<MdmDistributeLog> list = integrationService.listRecord(mdmDistributeLog);
        return getDataTable(list);
    }

    /** 分发日志失败重推（payload 完整保留，不截断） */
    @PreAuthorize("@ss.hasPermi('mdm:integration:log:edit')")
    @Log(title = "分发日志重推", businessType = BusinessType.UPDATE)
    @PutMapping("/distribute/retry/{recordId}")
    public AjaxResult retry(@PathVariable Long recordId)
    {
        return toAjax(integrationService.retryRecord(recordId));
    }

    /** 手动清理日志（type: receive|query|distribute，删除截止时间前日志） */
    @PreAuthorize("@ss.hasPermi('mdm:integration:log:remove')")
    @Log(title = "集成日志清理", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    public AjaxResult clean(@RequestParam String type, @RequestParam String beforeTime)
    {
        return toAjax(integrationService.cleanLog(type, beforeTime));
    }
}
