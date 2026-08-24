package com.ruoyi.mdm.relation.mapper;

import java.util.List;
import com.ruoyi.mdm.relation.domain.MdmRelation;

/**
 * 主数据对象关系Mapper接口
 *
 * @author ruoyi
 */
public interface MdmRelationMapper
{
    List<MdmRelation> selectMdmRelationList(MdmRelation relation);

    MdmRelation selectMdmRelationById(Long id);

    List<MdmRelation> selectBySourceObject(String sourceObjectCode);

    List<MdmRelation> selectByTargetObject(String targetObjectCode);

    int insertMdmRelation(MdmRelation relation);

    int updateMdmRelation(MdmRelation relation);

    int deleteMdmRelationById(Long id);

    int deleteMdmRelationByIds(Long[] ids);
}