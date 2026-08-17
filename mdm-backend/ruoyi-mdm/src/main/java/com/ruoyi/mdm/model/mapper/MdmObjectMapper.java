package com.ruoyi.mdm.model.mapper;

import java.util.List;
import com.ruoyi.mdm.model.domain.MdmObject;

/**
 * 主数据对象 数据层
 *
 * @author ruoyi
 */
public interface MdmObjectMapper
{
    /**
     * 查询主数据对象
     *
     * @param objectId 对象ID
     * @return 主数据对象
     */
    public MdmObject selectObjectById(Long objectId);

    /**
     * 查询主数据对象列表
     *
     * @param mdmObject 主数据对象
     * @return 主数据对象集合
     */
    public List<MdmObject> selectObjectList(MdmObject mdmObject);

    /**
     * 校验对象编码是否唯一
     *
     * @param objectCode 对象编码
     * @return 主数据对象
     */
    public MdmObject checkObjectCodeUnique(String objectCode);

    /**
     * 新增主数据对象
     *
     * @param mdmObject 主数据对象
     * @return 结果
     */
    public int insertObject(MdmObject mdmObject);

    /**
     * 修改主数据对象
     *
     * @param mdmObject 主数据对象
     * @return 结果
     */
    public int updateObject(MdmObject mdmObject);

    /**
     * 删除主数据对象
     *
     * @param objectId 对象ID
     * @return 结果
     */
    public int deleteObjectById(Long objectId);

    /**
     * 批量删除主数据对象
     *
     * @param objectIds 需要删除的对象ID
     * @return 结果
     */
    public int deleteObjectByIds(Long[] objectIds);
}
