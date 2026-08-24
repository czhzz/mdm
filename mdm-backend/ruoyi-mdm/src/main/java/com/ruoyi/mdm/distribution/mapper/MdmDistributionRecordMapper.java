package com.ruoyi.mdm.distribution.mapper;

import java.util.List;
import com.ruoyi.mdm.distribution.domain.MdmDistributionRecord;

/**
 * 主数据分发记录 数据层
 *
 * @author ruoyi
 */
public interface MdmDistributionRecordMapper
{
    public MdmDistributionRecord selectRecordById(Long recordId);

    public List<MdmDistributionRecord> selectRecordList(MdmDistributionRecord mdmDistributionRecord);

    public int insertRecord(MdmDistributionRecord mdmDistributionRecord);

    /** 更新推送结果（发送/成功/失败状态） */
    public int updatePushResult(MdmDistributionRecord mdmDistributionRecord);

    /** 订阅方确认回执 */
    public int updateConfirm(MdmDistributionRecord mdmDistributionRecord);

    /** 最近 N 条记录（监控统计用，1.1.0） */
    public List<MdmDistributionRecord> selectRecentRecords(int limit);
}