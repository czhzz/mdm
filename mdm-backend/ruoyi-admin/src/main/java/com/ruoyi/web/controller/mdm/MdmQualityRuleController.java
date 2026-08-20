package com.ruoyi.web.controller.mdm;

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
import com.ruoyi.mdm.quality.domain.MdmQualityRule;
import com.ruoyi.mdm.quality.service.IMdmQualityRuleService;

/**
 * 数据质量校验规则 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/quality/rule")
public class MdmQualityRuleController extends BaseController
{
    @Autowired
    private IMdmQualityRuleService ruleService;

    /**
     * 查询校验规则列表
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdmQualityRule mdmQualityRule)
    {
        startPage();
        List<MdmQualityRule> list = ruleService.selectRuleList(mdmQualityRule);
        return getDataTable(list);
    }

    /**
     * 获取校验规则详细信息
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:query')")
    @GetMapping(value = "/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId)
    {
        return success(ruleService.selectRuleById(ruleId));
    }

    /**
     * 新增校验规则
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:add')")
    @Log(title = "数据质量校验规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MdmQualityRule mdmQualityRule)
    {
        return toAjax(ruleService.insertRule(mdmQualityRule));
    }

    /**
     * 修改校验规则
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:edit')")
    @Log(title = "数据质量校验规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MdmQualityRule mdmQualityRule)
    {
        return toAjax(ruleService.updateRule(mdmQualityRule));
    }

    /**
     * 删除校验规则
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:remove')")
    @Log(title = "数据质量校验规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(ruleService.deleteRuleByIds(ruleIds));
    }
}
