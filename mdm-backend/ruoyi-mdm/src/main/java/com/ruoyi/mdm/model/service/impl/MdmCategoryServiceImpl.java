package com.ruoyi.mdm.model.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mdm.model.domain.MdmCategory;
import com.ruoyi.mdm.model.mapper.MdmCategoryMapper;
import com.ruoyi.mdm.model.service.IMdmCategoryService;

/**
 * 主数据分类 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmCategoryServiceImpl implements IMdmCategoryService
{
    @Autowired
    private MdmCategoryMapper categoryMapper;

    @Override
    public MdmCategory selectCategoryById(Long categoryId)
    {
        return categoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public List<MdmCategory> selectCategoryList(MdmCategory mdmCategory)
    {
        return categoryMapper.selectCategoryList(mdmCategory);
    }

    @Override
    public int insertCategory(MdmCategory mdmCategory)
    {
        mdmCategory.setCreateBy(SecurityUtils.getUsername());
        // 计算祖级列表
        MdmCategory info = categoryMapper.selectCategoryById(mdmCategory.getParentId());
        if (StringUtils.isNotNull(info))
        {
            String ancestors = info.getAncestors() + "," + info.getCategoryId();
            mdmCategory.setAncestors(ancestors);
        }
        else
        {
            mdmCategory.setAncestors("0");
        }
        return categoryMapper.insertCategory(mdmCategory);
    }

    @Override
    public int updateCategory(MdmCategory mdmCategory)
    {
        mdmCategory.setUpdateBy(SecurityUtils.getUsername());
        return categoryMapper.updateCategory(mdmCategory);
    }

    @Override
    public int deleteCategoryById(Long categoryId)
    {
        if (categoryMapper.hasChildByCategoryId(categoryId) > 0)
        {
            throw new ServiceException("存在子分类，不允许删除");
        }
        return categoryMapper.deleteCategoryById(categoryId);
    }

    @Override
    public boolean checkCategoryCodeUnique(MdmCategory mdmCategory)
    {
        Long categoryId = StringUtils.isNull(mdmCategory.getCategoryId()) ? -1L : mdmCategory.getCategoryId();
        MdmCategory info = categoryMapper.checkCategoryCodeUnique(mdmCategory.getCategoryCode());
        if (StringUtils.isNotNull(info) && info.getCategoryId().longValue() != categoryId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
