package com.ruoyi.mdm.maintenance.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.maintenance.domain.MdmAuditFlow;
import com.ruoyi.mdm.maintenance.mapper.MdmAuditFlowMapper;
import com.ruoyi.mdm.maintenance.service.IMdmAuditFlowService;

/**
 * 审核流程配置 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmAuditFlowServiceImpl implements IMdmAuditFlowService
{
    @Autowired
    private MdmAuditFlowMapper flowMapper;

    @Override
    public MdmAuditFlow selectFlowByObjectId(Long objectId)
    {
        return flowMapper.selectFlowByObjectId(objectId);
    }

    @Override
    public int saveFlow(MdmAuditFlow mdmAuditFlow)
    {
        MdmAuditFlow exist = flowMapper.selectFlowByObjectId(mdmAuditFlow.getObjectId());
        if (StringUtils.isNotNull(exist))
        {
            mdmAuditFlow.setFlowId(exist.getFlowId());
            mdmAuditFlow.setUpdateBy(SecurityUtils.getUsername());
            return flowMapper.updateFlow(mdmAuditFlow);
        }
        mdmAuditFlow.setCreateBy(SecurityUtils.getUsername());
        return flowMapper.insertFlow(mdmAuditFlow);
    }
}
