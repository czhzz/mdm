package com.ruoyi.web.controller.mdm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mdm.maintenance.domain.MdmAuditFlow;
import com.ruoyi.mdm.maintenance.service.IMdmAuditFlowService;

/**
 * 主数据审核流程配置 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/audit/flow")
public class MdmAuditFlowController extends BaseController
{
    @Autowired
    private IMdmAuditFlowService flowService;

    /**
     * 按对象查询审核流程配置
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:query')")
    @GetMapping(value = "/{objectId}")
    public AjaxResult getInfo(@PathVariable Long objectId)
    {
        return success(flowService.selectFlowByObjectId(objectId));
    }

    /**
     * 保存审核流程配置（启用/禁用 + 审核角色）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:edit')")
    @Log(title = "审核流程配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult save(@RequestBody MdmAuditFlow mdmAuditFlow)
    {
        return toAjax(flowService.saveFlow(mdmAuditFlow));
    }
}
