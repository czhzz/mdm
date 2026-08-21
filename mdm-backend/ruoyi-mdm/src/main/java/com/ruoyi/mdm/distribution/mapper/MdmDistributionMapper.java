package com.ruoyi.mdm.distribution.mapper;

import java.util.List;
import com.ruoyi.mdm.distribution.domain.MdmDistribution;

/**
 * 主数据分发配置 数据层
 *
 * @author ruoyi
 */
public interface MdmDistributionMapper
{
    public MdmDistribution selectDistById(Long distId);

    public List<MdmDistribution> selectDistList(MdmDistribution mdmDistribution);

    /** 查询对象启用的即时分发配置（后台推送用） */
    public List<MdmDistribution> selectEnabledListByObjectId(Long objectId);

    public int insertDist(MdmDistribution mdmDistribution);

    public int updateDist(MdmDistribution mdmDistribution);

    public int deleteDistByIds(Long[] distIds);
}