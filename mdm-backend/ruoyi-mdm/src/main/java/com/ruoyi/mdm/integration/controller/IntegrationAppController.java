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
import com.ruoyi.mdm.integration.domain.MdmApp;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成管理-应用凭证 操作处理（1.2.0 自 distribution 迁入）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/integration/app")
public class IntegrationAppController extends BaseController
{
    @Autowired
    private IIntegrationService integrationService;

    @PreAuthorize("@ss.hasPermi('mdm:integration:app:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdmApp mdmApp)
    {
        startPage();
        List<MdmApp> list = integrationService.listApp(mdmApp);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:app:query')")
    @GetMapping("/{appId}")
    public AjaxResult getInfo(@PathVariable Long appId)
    {
        return success(integrationService.getApp(appId));
    }

    /** 新增应用，返回含 appid/secret 的应用（凭据仅此一次可见） */
    @PreAuthorize("@ss.hasPermi('mdm:integration:app:add')")
    @Log(title = "应用凭证", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MdmApp mdmApp)
    {
        return success(integrationService.addApp(mdmApp));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:app:edit')")
    @Log(title = "应用凭证", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MdmApp mdmApp)
    {
        return toAjax(integrationService.editApp(mdmApp));
    }

    /** 重置密钥（返回新 secret，仅展示一次） */
    @PreAuthorize("@ss.hasPermi('mdm:integration:app:edit')")
    @Log(title = "重置应用密钥", businessType = BusinessType.UPDATE)
    @PutMapping("/reset/{appId}")
    public AjaxResult resetSecret(@PathVariable Long appId)
    {
        return success(integrationService.resetSecret(appId));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:app:remove')")
    @Log(title = "应用凭证", businessType = BusinessType.DELETE)
    @DeleteMapping("/{appIds}")
    public AjaxResult remove(@PathVariable Long[] appIds)
    {
        return toAjax(integrationService.deleteApps(appIds));
    }
}
