package com.ruoyi.mdm.coderule.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.ruoyi.mdm.coderule.domain.MdmCodeRule;
import com.ruoyi.mdm.coderule.service.IMdmCodeRuleService;

/**
 * 主数据编码规则 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/coderule/rule")
public class MdmCodeRuleController extends BaseController
{
    @Autowired
    private IMdmCodeRuleService ruleService;

    /**
     * 查询编码规则列表
     */
    @PreAuthorize("@ss.hasPermi('mdm:coderule:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdmCodeRule mdmCodeRule)
    {
        startPage();
        List<MdmCodeRule> list = ruleService.selectRuleList(mdmCodeRule);
        return getDataTable(list);
    }

    /**
     * 获取编码规则详细信息（含分段）
     */
    @PreAuthorize("@ss.hasPermi('mdm:coderule:query')")
    @GetMapping(value = "/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId)
    {
        return success(ruleService.selectRuleById(ruleId));
    }

    /**
     * 新增编码规则
     */
    @PreAuthorize("@ss.hasPermi('mdm:coderule:add')")
    @Log(title = "编码规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MdmCodeRule mdmCodeRule)
    {
        if (!ruleService.checkRuleUnique(mdmCodeRule))
        {
            return error("该对象已配置编码方案");
        }
        return toAjax(ruleService.insertRule(mdmCodeRule));
    }

    /**
     * 修改编码规则
     */
    @PreAuthorize("@ss.hasPermi('mdm:coderule:edit')")
    @Log(title = "编码规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MdmCodeRule mdmCodeRule)
    {
        if (!ruleService.checkRuleUnique(mdmCodeRule))
        {
            return error("该对象已配置编码方案");
        }
        return toAjax(ruleService.updateRule(mdmCodeRule));
    }

    /**
     * 删除编码规则
     */
    @PreAuthorize("@ss.hasPermi('mdm:coderule:remove')")
    @Log(title = "编码规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleId}")
    public AjaxResult remove(@PathVariable Long ruleId)
    {
        return toAjax(ruleService.deleteRuleById(ruleId));
    }

    /**
     * 预览示例编码
     */
    @PreAuthorize("@ss.hasPermi('mdm:coderule:query')")
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody MdmCodeRule mdmCodeRule)
    {
        return success(ruleService.previewCode(mdmCodeRule));
    }
}
