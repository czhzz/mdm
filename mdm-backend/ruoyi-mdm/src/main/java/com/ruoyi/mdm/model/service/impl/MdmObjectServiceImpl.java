package com.ruoyi.mdm.model.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmAttributeMapper;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;
import com.ruoyi.mdm.model.service.IMdmObjectService;

/**
 * 主数据对象 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmObjectServiceImpl implements IMdmObjectService
{
    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private MdmAttributeMapper attributeMapper;

    @Override
    public MdmObject selectObjectById(Long objectId)
    {
        return objectMapper.selectObjectById(objectId);
    }

    @Override
    public List<MdmObject> selectObjectList(MdmObject mdmObject)
    {
        return objectMapper.selectObjectList(mdmObject);
    }

    @Override
    public int insertObject(MdmObject mdmObject)
    {
        mdmObject.setCreateBy(SecurityUtils.getUsername());
        return objectMapper.insertObject(mdmObject);
    }

    @Override
    public int updateObject(MdmObject mdmObject)
    {
        mdmObject.setUpdateBy(SecurityUtils.getUsername());
        return objectMapper.updateObject(mdmObject);
    }

    @Override
    public int deleteObjectByIds(Long[] objectIds)
    {
        // 级联删除对象属性
        attributeMapper.deleteAttributeByObjectIds(objectIds);
        return objectMapper.deleteObjectByIds(objectIds);
    }

    @Override
    public boolean checkObjectCodeUnique(MdmObject mdmObject)
    {
        Long objectId = StringUtils.isNull(mdmObject.getObjectId()) ? -1L : mdmObject.getObjectId();
        MdmObject info = objectMapper.checkObjectCodeUnique(mdmObject.getObjectCode());
        if (StringUtils.isNotNull(info) && info.getObjectId().longValue() != objectId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
