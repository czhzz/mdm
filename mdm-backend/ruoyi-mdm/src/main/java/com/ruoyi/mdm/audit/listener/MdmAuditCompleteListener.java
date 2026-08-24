package com.ruoyi.mdm.audit.listener;

import java.util.Map;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;

/**
 * Flowable 审核完成监听器——审批通过时落地数据
 *
 * @author ruoyi
 */
@Component("mdmAuditCompleteListener")
public class MdmAuditCompleteListener implements TaskListener
{
    private static final long serialVersionUID = 1L;

    @Autowired
    private IMdmDataService dataService;

    @Override
    public void notify(DelegateTask delegateTask)
    {
        // 获取 Flowable 变量
        Map<String, Object> vars = delegateTask.getVariables();
        String objectCode = (String) vars.get("objectCode");
        Long dataId = vars.get("dataId") != null ? Long.valueOf(String.valueOf(vars.get("dataId"))) : null;
        String actionType = (String) vars.get("actionType");
        Boolean approved = (Boolean) vars.getOrDefault("approved", true);

        if (approved != null && approved && objectCode != null)
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) vars.get("data");
            if ("INSERT".equals(actionType))
            {
                dataService.applyAuditInsert(objectCode, data);
            }
            else if ("UPDATE".equals(actionType) && dataId != null)
            {
                dataService.applyAuditUpdate(objectCode, dataId, data);
            }
        }
    }
}