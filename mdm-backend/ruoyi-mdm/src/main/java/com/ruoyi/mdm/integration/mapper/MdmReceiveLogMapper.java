package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import com.ruoyi.mdm.integration.domain.MdmReceiveLog;

/**
 * 接收接口日志 数据层（mdm_receive_log）
 *
 * @author ruoyi
 */
public interface MdmReceiveLogMapper
{
    public List<MdmReceiveLog> selectList(MdmReceiveLog mdmReceiveLog);

    public int insert(MdmReceiveLog mdmReceiveLog);

    /** 手动清理：删除截止时间前的日志（页面二次确认） */
    public int deleteBeforeTime(String beforeTime);
}
