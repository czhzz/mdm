package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import com.ruoyi.mdm.integration.domain.MdmDistributeLog;

/**
 * 主数据分发日志 数据层（mdm_distribute_log）
 *
 * @author ruoyi
 */
public interface MdmDistributeLogMapper
{
    public MdmDistributeLog selectRecordById(Long recordId);

    public List<MdmDistributeLog> selectRecordList(MdmDistributeLog mdmDistributeLog);

    public int insertRecord(MdmDistributeLog mdmDistributeLog);

    /** 更新推送结果（发送/成功/失败状态） */
    public int updatePushResult(MdmDistributeLog mdmDistributeLog);

    /** 订阅方确认回执 */
    public int updateConfirm(MdmDistributeLog mdmDistributeLog);

    /** 最近 N 条记录（监控统计用，1.1.0） */
    public List<MdmDistributeLog> selectRecentRecords(int limit);

    /** 手动清理：删除截止时间前的记录（页面二次确认） */
    public int deleteBeforeTime(String beforeTime);
}
