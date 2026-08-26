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
import com.ruoyi.mdm.integration.domain.MdmReceiveApi;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成管理-接收接口配置 操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/integration/receive")
public class IntegrationReceiveController extends BaseController
{
    @Autowired
    private IIntegrationService integrationService;

    @PreAuthorize("@ss.hasPermi('mdm:integration:receive:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdmReceiveApi mdmReceiveApi)
    {
        startPage();
        List<MdmReceiveApi> list = integrationService.listReceiveApi(mdmReceiveApi);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:receive:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(integrationService.getReceiveApi(id));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:receive:add')")
    @Log(title = "接收接口", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MdmReceiveApi mdmReceiveApi)
    {
        return toAjax(integrationService.addReceiveApi(mdmReceiveApi));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:receive:edit')")
    @Log(title = "接收接口", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MdmReceiveApi mdmReceiveApi)
    {
        return toAjax(integrationService.editReceiveApi(mdmReceiveApi));
    }

    @PreAuthorize("@ss.hasPermi('mdm:integration:receive:remove')")
    @Log(title = "接收接口", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(integrationService.deleteReceiveApis(ids));
    }
}
