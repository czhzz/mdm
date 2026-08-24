package com.ruoyi.mdm.relation.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mdm.relation.domain.MdmRelation;
import com.ruoyi.mdm.relation.service.IMdmRelationService;

/**
 * 主数据对象关系Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/relation")
public class MdmRelationController extends BaseController
{
    @Autowired
    private IMdmRelationService mdmRelationService;

    @GetMapping("/list")
    public TableDataInfo list(MdmRelation relation)
    {
        startPage();
        List<MdmRelation> list = mdmRelationService.selectMdmRelationList(relation);
        return getDataTable(list);
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(mdmRelationService.selectMdmRelationById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody MdmRelation relation)
    {
        return toAjax(mdmRelationService.insertMdmRelation(relation));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody MdmRelation relation)
    {
        return toAjax(mdmRelationService.updateMdmRelation(relation));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(mdmRelationService.deleteMdmRelationByIds(ids));
    }
}