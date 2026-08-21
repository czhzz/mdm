package com.ruoyi.mdm.maintenance.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.maintenance.domain.MdmAuditTask;
import com.ruoyi.mdm.maintenance.mapper.MdmAuditTaskMapper;
import com.ruoyi.mdm.maintenance.service.IMdmAuditService;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

/**
 * 主数据审核 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmAuditServiceImpl implements IMdmAuditService
{
    @Autowired
    private MdmAuditTaskMapper taskMapper;

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private IMdmDataService dataService;

    @Override
    public List<MdmAuditTask> listTask(MdmAuditTask mdmAuditTask)
    {
        return taskMapper.selectTaskList(mdmAuditTask);
    }

    @Override
    @Transactional
    public int submitAudit(String objectCode, Long dataId, String actionType, Map<String, Object> afterData)
    {
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (StringUtils.isNull(object))
        {
            throw new ServiceException("对象不存在：" + objectCode);
        }
        if (!"INSERT".equals(actionType) && !"UPDATE".equals(actionType))
        {
            throw new ServiceException("不支持的操作类型：" + actionType);
        }
        MdmAuditTask task = new MdmAuditTask();
        task.setObjectId(object.getObjectId());
        task.setDataId(dataId == null ? 0L : dataId);
        task.setActionType(actionType);
        task.setStatus("0");
        task.setSubmitBy(SecurityUtils.getUsername());
        if ("UPDATE".equals(actionType) && dataId != null && dataId > 0)
        {
            Map<String, Object> before = dataService.selectDataById(objectCode, dataId);
            task.setBeforeData(JSON.toJSONString(before));
        }
        task.setAfterData(JSON.toJSONString(afterData));
        task.setCreateBy(SecurityUtils.getUsername());
        return taskMapper.insertTask(task);
    }

    @Override
    @Transactional
    public int approve(Long taskId)
    {
        MdmAuditTask task = taskMapper.selectTaskById(taskId);
        if (StringUtils.isNull(task))
        {
            throw new ServiceException("审核任务不存在");
        }
        if (!"0".equals(task.getStatus()))
        {
            throw new ServiceException("该任务已处理");
        }
        MdmObject object = objectMapper.selectObjectById(task.getObjectId());
        if (StringUtils.isNull(object))
        {
            throw new ServiceException("对象不存在");
        }
        String objectCode = object.getObjectCode();
        Map<String, Object> afterData = JSON.parseObject(task.getAfterData());
        if ("INSERT".equals(task.getActionType()))
        {
            dataService.applyAuditInsert(objectCode, afterData);
        }
        else if ("UPDATE".equals(task.getActionType()))
        {
            dataService.applyAuditUpdate(objectCode, task.getDataId(), afterData);
        }
        MdmAuditTask update = new MdmAuditTask();
        update.setTaskId(taskId);
        update.setStatus("1");
        update.setAuditBy(SecurityUtils.getUsername());
        return taskMapper.updateTaskResult(update);
    }

    @Override
    @Transactional
    public int reject(Long taskId, String reason)
    {
        MdmAuditTask task = taskMapper.selectTaskById(taskId);
        if (StringUtils.isNull(task))
        {
            throw new ServiceException("审核任务不存在");
        }
        if (!"0".equals(task.getStatus()))
        {
            throw new ServiceException("该任务已处理");
        }
        MdmAuditTask update = new MdmAuditTask();
        update.setTaskId(taskId);
        update.setStatus("2");
        update.setRejectReason(reason);
        update.setAuditBy(SecurityUtils.getUsername());
        return taskMapper.updateTaskResult(update);
    }
}
