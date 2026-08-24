package com.ruoyi.mdm.template.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mdm.template.service.IMdmTemplateService;

/**
 * 主数据对象模板Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/template")
public class MdmTemplateController extends BaseController
{
    @Autowired
    private IMdmTemplateService mdmTemplateService;

    @GetMapping("/list")
    public AjaxResult list()
    {
        return success(mdmTemplateService.listTemplates());
    }

    @GetMapping("/preview/{code}")
    public AjaxResult preview(@PathVariable("code") String code)
    {
        return success(mdmTemplateService.previewTemplate(code));
    }

    @PostMapping("/create/{code}")
    public AjaxResult create(@PathVariable("code") String code)
    {
        return toAjax(mdmTemplateService.createFromTemplate(code));
    }
}