package com.ruoyi.mdm.quality.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mdm.quality.domain.MdmQualityIssue;

/**
 * 数据质量 服务层（台账 + 重复检测）
 *
 * @author ruoyi
 */
public interface IMdmQualityService
{
    /**
     * 查询质量问题列表
     *
     * @param mdmQualityIssue 质量问题
     * @return 质量问题集合
     */
    public List<MdmQualityIssue> selectIssueList(MdmQualityIssue mdmQualityIssue);

    /**
     * 处理质量问题（标记已处理/忽略）
     *
     * @param mdmQualityIssue 质量问题（含 issueId 与 handleStatus）
     * @return 结果
     */
    public int handleIssue(MdmQualityIssue mdmQualityIssue);

    /**
     * 重复检测（按字段分组，返回重复组并自动登记台账）
     *
     * @param objectCode 对象编码
     * @param fields 查重字段（属性编码）
     * @return 重复数据组列表
     */
    public List<Map<String, Object>> duplicateCheck(String objectCode, List<String> fields);

    /**
     * 获取质量大屏聚合数据（1.1.0）
     *
     * @return 总览卡片 + 趋势 + 排行 + 规则分布 + 最近问题
     */
    public Map<String, Object> getDashboardData();
}
