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
import com.ruoyi.mdm.model.domain.MdmCategory;
import com.ruoyi.mdm.model.service.IMdmCategoryService;

/**
 * 主数据分类 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/model/category")
public class MdmCategoryController extends BaseController
{
    @Autowired
    private IMdmCategoryService categoryService;

    /**
     * 查询主数据分类列表（树，全量）
     */
    @PreAuthorize("@ss.hasPermi('mdm:category:query')")
    @GetMapping("/list")
    public AjaxResult list(MdmCategory mdmCategory)
    {
        List<MdmCategory> list = categoryService.selectCategoryList(mdmCategory);
        return success(list);
    }

    /**
     * 获取主数据分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('mdm:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(categoryService.selectCategoryById(categoryId));
    }

    /**
     * 新增主数据分类
     */
    @PreAuthorize("@ss.hasPermi('mdm:category:add')")
    @Log(title = "主数据分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MdmCategory mdmCategory)
    {
        if (!categoryService.checkCategoryCodeUnique(mdmCategory))
        {
            return error("新增分类'" + mdmCategory.getCategoryName() + "'失败，分类编码已存在");
        }
        return toAjax(categoryService.insertCategory(mdmCategory));
    }

    /**
     * 修改主数据分类
     */
    @PreAuthorize("@ss.hasPermi('mdm:category:edit')")
    @Log(title = "主数据分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MdmCategory mdmCategory)
    {
        if (!categoryService.checkCategoryCodeUnique(mdmCategory))
        {
            return error("修改分类'" + mdmCategory.getCategoryName() + "'失败，分类编码已存在");
        }
        return toAjax(categoryService.updateCategory(mdmCategory));
    }

    /**
     * 删除主数据分类
     */
    @PreAuthorize("@ss.hasPermi('mdm:category:remove')")
    @Log(title = "主数据分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(@PathVariable Long categoryId)
    {
        return toAjax(categoryService.deleteCategoryById(categoryId));
    }
}
