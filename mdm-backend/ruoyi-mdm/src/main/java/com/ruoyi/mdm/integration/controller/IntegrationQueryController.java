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
import com.ruoyi.mdm.integration.domain.MdmQueryApi;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成管理-查询接口配置 操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/integration/query")
public class IntegrationQueryController extends BaseController
{
    @Autowired
    private IIntegrationService integrationService;

    @PreAuthorize("@ss.hasPermi('mdm:integration:query:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdmQueryApi mdmQueryApi)
    {
        startPage();
        List<MdmQueryApi> list = integrationService.listQueryApi(mdmQueryApi);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:query:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(integrationService.getQueryApi(id));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:query:add')")
    @Log(title = "查询接口", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MdmQueryApi mdmQueryApi)
    {
        return toAjax(integrationService.addQueryApi(mdmQueryApi));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:query:edit')")
    @Log(title = "查询接口", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MdmQueryApi mdmQueryApi)
    {
        return toAjax(integrationService.editQueryApi(mdmQueryApi));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:query:remove')")
    @Log(title = "查询接口", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(integrationService.deleteQueryApis(ids));
    }
}
