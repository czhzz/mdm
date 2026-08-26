package com.ruoyi.mdm.integration.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mdm.integration.domain.MdmDistributeApi;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成管理-分发配置 操作处理（1.2.0 自 distribution 迁入，读写 mdm_distribute_api）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/integration/distribute")
public class IntegrationDistributeController extends BaseController
{
    @Autowired
    private IIntegrationService integrationService;

    @PreAuthorize("@ss.hasPermi('mdm:integration:distribute:list')")
    @GetMapping("/config/list")
    public TableDataInfo list(MdmDistributeApi mdmDistributeApi)
    {
        startPage();
        List<MdmDistributeApi> list = integrationService.listDist(mdmDistributeApi);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:distribute:query')")
    @GetMapping("/config/{distId}")
    public AjaxResult getInfo(@PathVariable Long distId)
    {
        return success(integrationService.getDist(distId));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:distribute:add')")
    @Log(title = "分发配置", businessType = BusinessType.INSERT)
    @PostMapping("/config")
    public AjaxResult add(@RequestBody MdmDistributeApi mdmDistributeApi)
    {
        return toAjax(integrationService.addDist(mdmDistributeApi));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:distribute:edit')")
    @Log(title = "分发配置", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult edit(@RequestBody MdmDistributeApi mdmDistributeApi)
    {
        return toAjax(integrationService.editDist(mdmDistributeApi));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:distribute:remove')")
    @Log(title = "分发配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/config/{distIds}")
    public AjaxResult remove(@PathVariable Long[] distIds)
    {
        return toAjax(integrationService.deleteDists(distIds));
    }

    /** 分发监控（MQ 通道状态/成功率） */
    @PreAuthorize("@ss.hasPermi('mdm:integration:distribute:list')")
    @GetMapping("/monitor")
    public AjaxResult monitor()
    {
        return success(integrationService.getMqMonitorData());
    }
}
