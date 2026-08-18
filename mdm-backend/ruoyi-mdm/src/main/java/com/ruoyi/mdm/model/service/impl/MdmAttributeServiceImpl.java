package com.ruoyi.mdm.model.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmAttributeMapper;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;
import com.ruoyi.mdm.model.service.IMdmAttributeService;

/**
 * 主数据属性 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmAttributeServiceImpl implements IMdmAttributeService
{
    @Autowired
    private MdmAttributeMapper attributeMapper;

    @Autowired
    private MdmObjectMapper objectMapper;

    @Override
    public MdmAttribute selectAttributeById(Long attrId)
    {
        return attributeMapper.selectAttributeById(attrId);
    }

    @Override
    public List<MdmAttribute> selectAttributeList(MdmAttribute mdmAttribute)
    {
        return attributeMapper.selectAttributeList(mdmAttribute);
    }

    @Override
    public int insertAttribute(MdmAttribute mdmAttribute)
    {
        mdmAttribute.setCreateBy(SecurityUtils.getUsername());
        return attributeMapper.insertAttribute(mdmAttribute);
    }

    @Override
    public int updateAttribute(MdmAttribute mdmAttribute)
    {
        mdmAttribute.setUpdateBy(SecurityUtils.getUsername());
        return attributeMapper.updateAttribute(mdmAttribute);
    }

    @Override
    public int deleteAttributeByIds(Long[] attrIds)
    {
        int rows = 0;
        for (Long attrId : attrIds)
        {
            MdmAttribute attr = attributeMapper.selectAttributeById(attrId);
            if (StringUtils.isNotNull(attr))
            {
                MdmObject object = objectMapper.selectObjectById(attr.getObjectId());
                if (StringUtils.isNotNull(object) && "1".equals(object.getStatus()))
                {
                    throw new ServiceException("已发布对象'" + object.getObjectName() + "'的属性不可删除");
                }
            }
            rows += attributeMapper.deleteAttributeById(attrId);
        }
        return rows;
    }

    @Override
    public boolean checkAttributeCodeUnique(MdmAttribute mdmAttribute)
    {
        Long attrId = StringUtils.isNull(mdmAttribute.getAttrId()) ? -1L : mdmAttribute.getAttrId();
        MdmAttribute info = attributeMapper.checkAttributeCodeUnique(mdmAttribute.getObjectId(), mdmAttribute.getAttrCode());
        if (StringUtils.isNotNull(info) && info.getAttrId().longValue() != attrId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
