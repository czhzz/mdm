package com.ruoyi.mdm.model.mapper;

import java.util.List;
import com.ruoyi.mdm.model.domain.MdmCategory;

/**
 * 主数据分类 数据层
 *
 * @author ruoyi
 */
public interface MdmCategoryMapper
{
    /**
     * 查询主数据分类
     *
     * @param categoryId 分类ID
     * @return 主数据分类
     */
    public MdmCategory selectCategoryById(Long categoryId);

    /**
     * 查询主数据分类列表
     *
     * @param mdmCategory 主数据分类
     * @return 主数据分类集合
     */
    public List<MdmCategory> selectCategoryList(MdmCategory mdmCategory);

    /**
     * 校验分类编码是否唯一
     *
     * @param categoryCode 分类编码
     * @return 主数据分类
     */
    public MdmCategory checkCategoryCodeUnique(String categoryCode);

    /**
     * 新增主数据分类
     *
     * @param mdmCategory 主数据分类
     * @return 结果
     */
    public int insertCategory(MdmCategory mdmCategory);

    /**
     * 修改主数据分类
     *
     * @param mdmCategory 主数据分类
     * @return 结果
     */
    public int updateCategory(MdmCategory mdmCategory);

    /**
     * 删除主数据分类
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    public int deleteCategoryById(Long categoryId);

    /**
     * 查询是否存在子分类
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    public int hasChildByCategoryId(Long categoryId);
}
