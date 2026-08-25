package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import com.ruoyi.mdm.integration.domain.MdmQueryLog;

/**
 * 查询接口日志 数据层（mdm_query_log）
 *
 * @author ruoyi
 */
public interface MdmQueryLogMapper
{
    public List<MdmQueryLog> selectList(MdmQueryLog mdmQueryLog);

    public int insert(MdmQueryLog mdmQueryLog);

    /** 手动清理：删除截止时间前的日志（页面二次确认） */
    public int deleteBeforeTime(String beforeTime);
}
