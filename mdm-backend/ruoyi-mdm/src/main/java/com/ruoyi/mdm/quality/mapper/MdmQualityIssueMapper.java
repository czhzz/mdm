package com.ruoyi.mdm.quality.mapper;

import java.util.List;
import com.ruoyi.mdm.quality.domain.MdmQualityIssue;

/**
 * 数据质量台账 数据层
 *
 * @author ruoyi
 */
public interface MdmQualityIssueMapper
{
    /**
     * 查询质量问题
     *
     * @param issueId 问题ID
     * @return 质量问题
     */
    public MdmQualityIssue selectIssueById(Long issueId);

    /**
     * 查询质量问题列表
     *
     * @param mdmQualityIssue 质量问题
     * @return 质量问题集合
     */
    public List<MdmQualityIssue> selectIssueList(MdmQualityIssue mdmQualityIssue);

    /**
     * 新增质量问题
     *
     * @param mdmQualityIssue 质量问题
     * @return 结果
     */
    public int insertIssue(MdmQualityIssue mdmQualityIssue);

    /**
     * 更新质量问题处理状态
     *
     * @param mdmQualityIssue 质量问题
     * @return 结果
     */
    public int updateIssueHandle(MdmQualityIssue mdmQualityIssue);

    /**
     * 删除质量问题
     *
     * @param issueId 问题ID
     * @return 结果
     */
    public int deleteIssueById(Long issueId);
}
