package com.ruoyi.mdm.model.service;

import java.util.List;
import com.ruoyi.mdm.model.domain.MdmAttribute;

/**
 * 主数据属性 服务层
 *
 * @author ruoyi
 */
public interface IMdmAttributeService
{
    /**
     * 查询主数据属性
     *
     * @param attrId 属性ID
     * @return 主数据属性
     */
    public MdmAttribute selectAttributeById(Long attrId);

    /**
     * 查询主数据属性列表
     *
     * @param mdmAttribute 主数据属性
     * @return 主数据属性集合
     */
    public List<MdmAttribute> selectAttributeList(MdmAttribute mdmAttribute);

    /**
     * 新增主数据属性
     *
     * @param mdmAttribute 主数据属性
     * @return 结果
     */
    public int insertAttribute(MdmAttribute mdmAttribute);

    /**
     * 修改主数据属性
     *
     * @param mdmAttribute 主数据属性
     * @return 结果
     */
    public int updateAttribute(MdmAttribute mdmAttribute);

    /**
     * 批量删除主数据属性
     *
     * @param attrIds 需要删除的属性ID
     * @return 结果
     */
    public int deleteAttributeByIds(Long[] attrIds);

    /**
     * 校验对象内属性编码是否唯一
     *
     * @param mdmAttribute 主数据属性
     * @return 结果（true唯一 false不唯一）
     */
    public boolean checkAttributeCodeUnique(MdmAttribute mdmAttribute);
}
