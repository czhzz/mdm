package com.ruoyi.mdm.relation.service;

import java.util.List;
import com.ruoyi.mdm.relation.domain.MdmRelation;

/**
 * 主数据对象关系Service接口
 *
 * @author ruoyi
 */
public interface IMdmRelationService
{
    List<MdmRelation> selectMdmRelationList(MdmRelation relation);

    MdmRelation selectMdmRelationById(Long id);

    List<MdmRelation> selectBySourceObject(String sourceObjectCode);

    List<MdmRelation> selectByTargetObject(String targetObjectCode);

    int insertMdmRelation(MdmRelation relation);

    int updateMdmRelation(MdmRelation relation);

    int deleteMdmRelationById(Long id);

    int deleteMdmRelationByIds(Long[] ids);

    /**
     * 检查目标数据是否被引用，返回引用关系列表
     */
    List<MdmRelation> checkTargetReferenced(String targetObjectCode, String targetDataCode);

    /**
     * 执行级联删除——根据级联规则删除源数据
     */
    void cascadeDelete(String targetObjectCode, String targetDataCode);
}