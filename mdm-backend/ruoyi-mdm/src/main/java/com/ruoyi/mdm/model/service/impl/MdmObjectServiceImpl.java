package com.ruoyi.mdm.model.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mdm.model.domain.MdmAttribute;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        for (Long objectId : objectIds)
        {
            MdmObject object = objectMapper.selectObjectById(objectId);
            if (StringUtils.isNotNull(object) && "1".equals(object.getStatus()))
            {
                throw new ServiceException("已发布对象'" + object.getObjectName() + "'不可删除，请先停用");
            }
        }
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

    @Override
    public int publishObject(Long objectId)
    {
        MdmObject object = objectMapper.selectObjectById(objectId);
        if (StringUtils.isNull(object))
        {
            throw new ServiceException("对象不存在");
        }
        // 表名/列名白名单校验（防 SQL 注入）
        if (!isValidIdentifier(object.getObjectCode()))
        {
            throw new ServiceException("对象编码包含非法字符，无法发布");
        }
        String tableName = "mdm_data_" + object.getObjectCode();
        if (checkTableExists(tableName))
        {
            throw new ServiceException("对象已发布过，数据表已存在");
        }
        // 查询该对象属性
        MdmAttribute query = new MdmAttribute();
        query.setObjectId(objectId);
        List<MdmAttribute> attrs = attributeMapper.selectAttributeList(query);
        if (attrs.isEmpty())
        {
            throw new ServiceException("至少需要一个属性才能发布");
        }
        for (MdmAttribute attr : attrs)
        {
            if (!isValidIdentifier(attr.getAttrCode()))
            {
                throw new ServiceException("属性编码包含非法字符：" + attr.getAttrCode());
            }
        }
        // 生成建表 SQL（属性列 + 通用列 + 唯一索引）
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(tableName).append(" (");
        ddl.append("id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键', ");
        ddl.append("object_code VARCHAR(50) NOT NULL COMMENT '对象编码', ");
        ddl.append("status CHAR(1) DEFAULT '0' COMMENT '状态(0草稿 1生效 2停用)', ");
        ddl.append("version INT DEFAULT 1 COMMENT '版本号', ");
        for (MdmAttribute attr : attrs)
        {
            ddl.append(attr.getAttrCode()).append(" ").append(columnType(attr.getDataType()));
            ddl.append("Y".equals(attr.getRequiredFlag()) ? " NOT NULL" : " DEFAULT NULL");
            ddl.append(" COMMENT '").append(attr.getAttrName().replace("'", "\\'")).append("', ");
        }
        ddl.append("create_by VARCHAR(64) DEFAULT '' COMMENT '创建者', ");
        ddl.append("create_time DATETIME COMMENT '创建时间', ");
        ddl.append("update_by VARCHAR(64) DEFAULT '' COMMENT '更新者', ");
        ddl.append("update_time DATETIME COMMENT '更新时间', ");
        ddl.append("remark VARCHAR(500) DEFAULT NULL COMMENT '备注', ");
        ddl.append("PRIMARY KEY (id), UNIQUE KEY uk_object_code (object_code)");
        for (MdmAttribute attr : attrs)
        {
            if ("Y".equals(attr.getUniqueFlag()))
            {
                ddl.append(", UNIQUE KEY uk_").append(attr.getAttrCode()).append(" (").append(attr.getAttrCode()).append(")");
            }
        }
        ddl.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主数据业务表'");
        jdbcTemplate.execute(ddl.toString());
        // 更新对象状态为已发布
        MdmObject update = new MdmObject();
        update.setObjectId(objectId);
        update.setStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        return objectMapper.updateObject(update);
    }

    /**
     * 表名/列名合法性校验：字母、数字、下划线，字母开头
     */
    private boolean isValidIdentifier(String name)
    {
        return StringUtils.isNotBlank(name) && name.matches("^[a-zA-Z_][a-zA-Z0-9_]{0,49}$");
    }

    /**
     * 属性数据类型 → MySQL 列类型
     */
    private String columnType(String dataType)
    {
        if ("number".equals(dataType))
        {
            return "DECIMAL(20,4)";
        }
        if ("date".equals(dataType))
        {
            return "DATE";
        }
        if ("datetime".equals(dataType))
        {
            return "DATETIME";
        }
        if ("boolean".equals(dataType))
        {
            return "CHAR(1)";
        }
        if ("dict".equals(dataType))
        {
            return "VARCHAR(100)";
        }
        if ("enum".equals(dataType))
        {
            return "VARCHAR(100)";
        }
        return "VARCHAR(255)"; // text
    }

    /**
     * 检查数据表是否存在
     */
    private boolean checkTableExists(String tableName)
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class, tableName);
        return count != null && count > 0;
    }
}
