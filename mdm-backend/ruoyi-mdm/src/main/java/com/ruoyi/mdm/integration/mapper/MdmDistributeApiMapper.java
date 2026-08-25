package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import com.ruoyi.mdm.integration.domain.MdmDistributeApi;

/**
 * 主数据分发配置 数据层（mdm_distribute_api）
 *
 * @author ruoyi
 */
public interface MdmDistributeApiMapper
{
    public MdmDistributeApi selectDistById(Long distId);

    public List<MdmDistributeApi> selectDistList(MdmDistributeApi mdmDistributeApi);

    /** 查询对象启用的即时分发配置（后台推送用） */
    public List<MdmDistributeApi> selectEnabledListByObjectId(Long objectId);

    public int insertDist(MdmDistributeApi mdmDistributeApi);

    public int updateDist(MdmDistributeApi mdmDistributeApi);

    public int deleteDistByIds(Long[] distIds);
}
