package com.ruoyi.mdm.relation.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mdm.relation.domain.MdmRelation;
import com.ruoyi.mdm.relation.mapper.MdmRelationMapper;
import com.ruoyi.mdm.relation.service.IMdmRelationService;

/**
 * 主数据对象关系Service实现
 *
 * @author ruoyi
 */
@Service
public class MdmRelationServiceImpl implements IMdmRelationService
{
    @Autowired
    private MdmRelationMapper mdmRelationMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<MdmRelation> selectMdmRelationList(MdmRelation relation)
    {
        return mdmRelationMapper.selectMdmRelationList(relation);
    }

    @Override
    public MdmRelation selectMdmRelationById(Long id)
    {
        return mdmRelationMapper.selectMdmRelationById(id);
    }

    @Override
    public List<MdmRelation> selectBySourceObject(String sourceObjectCode)
    {
        return mdmRelationMapper.selectBySourceObject(sourceObjectCode);
    }

    @Override
    public List<MdmRelation> selectByTargetObject(String targetObjectCode)
    {
        return mdmRelationMapper.selectByTargetObject(targetObjectCode);
    }

    @Override
    public int insertMdmRelation(MdmRelation relation)
    {
        return mdmRelationMapper.insertMdmRelation(relation);
    }

    @Override
    public int updateMdmRelation(MdmRelation relation)
    {
        return mdmRelationMapper.updateMdmRelation(relation);
    }

    @Override
    public int deleteMdmRelationById(Long id)
    {
        return mdmRelationMapper.deleteMdmRelationById(id);
    }

    @Override
    public int deleteMdmRelationByIds(Long[] ids)
    {
        return mdmRelationMapper.deleteMdmRelationByIds(ids);
    }

    @Override
    public List<MdmRelation> checkTargetReferenced(String targetObjectCode, String targetDataCode)
    {
        // 查询所有源对象是 targetObjectCode 的关系
        MdmRelation query = new MdmRelation();
        query.setTargetObjectCode(targetObjectCode);
        return mdmRelationMapper.selectMdmRelationList(query);
    }

    @Override
    @Transactional
    public void cascadeDelete(String targetObjectCode, String targetDataCode)
    {
        // 查询所有指向该目标对象的关系
        List<MdmRelation> relations = checkTargetReferenced(targetObjectCode, targetDataCode);

        for (MdmRelation rel : relations)
        {
            String tableName = "mdm_data_" + rel.getSourceObjectCode();
            String fieldCode = rel.getSourceFieldCode();
            String cascadeRule = rel.getCascadeRule();

            if ("CASCADE".equals(cascadeRule))
            {
                // 级联删除：删除源对象中引用该目标数据的所有记录
                String sql = "DELETE FROM " + tableName + " WHERE " + fieldCode + " = ?";
                jdbcTemplate.update(sql, targetDataCode);
            }
            else if ("SET_NULL".equals(cascadeRule))
            {
                // 置空：将引用字段置为 NULL
                String sql = "UPDATE " + tableName + " SET " + fieldCode + " = NULL WHERE " + fieldCode + " = ?";
                jdbcTemplate.update(sql, targetDataCode);
            }
            // RESTRICT：由调用方在删除前检查，阻止删除
        }
    }
}