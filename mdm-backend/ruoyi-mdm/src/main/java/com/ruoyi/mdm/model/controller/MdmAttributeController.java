package com.ruoyi.mdm.model.controller;

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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.service.IMdmAttributeService;

/**
 * 主数据属性 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/model/attribute")
public class MdmAttributeController extends BaseController
{
    @Autowired
    private IMdmAttributeService attributeService;

    /**
     * 查询主数据属性列表（按对象，全量）
     */
    @PreAuthorize("@ss.hasPermi('mdm:attribute:query')")
    @GetMapping("/list")
    public AjaxResult list(MdmAttribute mdmAttribute)
    {
        List<MdmAttribute> list = attributeService.selectAttributeList(mdmAttribute);
        return success(list);
    }

    /**
     * 获取主数据属性详细信息
     */
    @PreAuthorize("@ss.hasPermi('mdm:attribute:query')")
    @GetMapping(value = "/{attrId}")
    public AjaxResult getInfo(@PathVariable Long attrId)
    {
        return success(attributeService.selectAttributeById(attrId));
    }

    /**
     * 新增主数据属性
     */
    @PreAuthorize("@ss.hasPermi('mdm:attribute:add')")
    @Log(title = "主数据属性", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MdmAttribute mdmAttribute)
    {
        if (!attributeService.checkAttributeCodeUnique(mdmAttribute))
        {
            return error("新增属性'" + mdmAttribute.getAttrName() + "'失败，对象内属性编码已存在");
        }
        return toAjax(attributeService.insertAttribute(mdmAttribute));
    }

    /**
     * 修改主数据属性
     */
    @PreAuthorize("@ss.hasPermi('mdm:attribute:edit')")
    @Log(title = "主数据属性", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MdmAttribute mdmAttribute)
    {
        if (!attributeService.checkAttributeCodeUnique(mdmAttribute))
        {
            return error("修改属性'" + mdmAttribute.getAttrName() + "'失败，对象内属性编码已存在");
        }
        return toAjax(attributeService.updateAttribute(mdmAttribute));
    }

    /**
     * 删除主数据属性
     */
    @PreAuthorize("@ss.hasPermi('mdm:attribute:remove')")
    @Log(title = "主数据属性", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attrIds}")
    public AjaxResult remove(@PathVariable Long[] attrIds)
    {
        return toAjax(attributeService.deleteAttributeByIds(attrIds));
    }
}
