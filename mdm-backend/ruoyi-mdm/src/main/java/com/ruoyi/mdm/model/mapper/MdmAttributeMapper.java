package com.ruoyi.mdm.model.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mdm.model.domain.MdmAttribute;

/**
 * 主数据属性 数据层
 *
 * @author ruoyi
 */
public interface MdmAttributeMapper
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
     * 校验对象内属性编码是否唯一
     *
     * @param objectId 对象ID
     * @param attrCode 属性编码
     * @return 主数据属性
     */
    public MdmAttribute checkAttributeCodeUnique(@Param("objectId") Long objectId, @Param("attrCode") String attrCode);

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
     * 删除主数据属性
     *
     * @param attrId 属性ID
     * @return 结果
     */
    public int deleteAttributeById(Long attrId);

    /**
     * 按对象ID批量删除属性
     *
     * @param objectIds 需要删除的对象ID
     * @return 结果
     */
    public int deleteAttributeByObjectIds(Long[] objectIds);
}
