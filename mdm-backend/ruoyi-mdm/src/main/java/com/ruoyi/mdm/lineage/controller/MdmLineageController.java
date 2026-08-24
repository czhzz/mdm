package com.ruoyi.mdm.lineage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mdm.lineage.service.IMdmLineageService;

/**
 * 主数据血缘追踪Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/lineage")
public class MdmLineageController extends BaseController
{
    @Autowired
    private IMdmLineageService lineageService;

    /**
     * 查询数据血缘（来源 → 数据 → 去向）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:query')")
    @GetMapping("/{objectCode}/{dataId}")
    public AjaxResult getLineage(@PathVariable String objectCode, @PathVariable Long dataId)
    {
        return success(lineageService.getLineage(objectCode, dataId));
    }
}