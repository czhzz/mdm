package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import com.ruoyi.mdm.integration.domain.MdmReceiveApi;

/**
 * 接收接口配置 数据层（mdm_receive_api）
 *
 * @author ruoyi
 */
public interface MdmReceiveApiMapper
{
    public MdmReceiveApi selectById(Long id);

    public MdmReceiveApi selectByApiCode(String apiCode);

    public List<MdmReceiveApi> selectList(MdmReceiveApi mdmReceiveApi);

    public int insert(MdmReceiveApi mdmReceiveApi);

    public int update(MdmReceiveApi mdmReceiveApi);

    public int deleteByIds(Long[] ids);
}
