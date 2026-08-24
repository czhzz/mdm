package com.ruoyi.web.controller.mdm;

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
import com.ruoyi.mdm.distribution.domain.MdmApp;
import com.ruoyi.mdm.distribution.domain.MdmDistribution;
import com.ruoyi.mdm.distribution.domain.MdmDistributionRecord;
import com.ruoyi.mdm.distribution.service.IDistributionService;

/**
 * 主数据分发与集成 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/distribution")
public class MdmDistributionController extends BaseController
{
    @Autowired
    private IDistributionService distributionService;

    // ===== 应用凭证 =====

    @PreAuthorize("@ss.hasPermi('mdm:distribution:list')")
    @GetMapping("/app/list")
    public TableDataInfo listApp(MdmApp mdmApp)
    {
        startPage();
        List<MdmApp> list = distributionService.listApp(mdmApp);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:query')")
    @GetMapping("/app/{appId}")
    public AjaxResult getApp(@PathVariable Long appId)
    {
        return success(distributionService.getApp(appId));
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:add')")
    @Log(title = "应用凭证", businessType = BusinessType.INSERT)
    @PostMapping("/app")
    public AjaxResult addApp(@RequestBody MdmApp mdmApp)
    {
        // 返回创建后的应用（含一次性的 appid/secret 供前端展示保存）
        return success(distributionService.addApp(mdmApp));
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:edit')")
    @Log(title = "应用凭证", businessType = BusinessType.UPDATE)
    @PutMapping("/app")
    public AjaxResult editApp(@RequestBody MdmApp mdmApp)
    {
        return toAjax(distributionService.editApp(mdmApp));
    }

    /** 重置密钥（返回新的 secret，仅展示一次） */
    @PreAuthorize("@ss.hasPermi('mdm:distribution:edit')")
    @Log(title = "重置应用密钥", businessType = BusinessType.UPDATE)
    @PutMapping("/app/reset/{appId}")
    public AjaxResult resetSecret(@PathVariable Long appId)
    {
        return success(distributionService.resetSecret(appId));
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:remove')")
    @Log(title = "应用凭证", businessType = BusinessType.DELETE)
    @DeleteMapping("/app/{appIds}")
    public AjaxResult deleteApps(@PathVariable Long[] appIds)
    {
        return toAjax(distributionService.deleteApps(appIds));
    }

    // ===== 分发配置 =====

    @PreAuthorize("@ss.hasPermi('mdm:distribution:list')")
    @GetMapping("/config/list")
    public TableDataInfo listDist(MdmDistribution mdmDistribution)
    {
        startPage();
        List<MdmDistribution> list = distributionService.listDist(mdmDistribution);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:query')")
    @GetMapping("/config/{distId}")
    public AjaxResult getDist(@PathVariable Long distId)
    {
        return success(distributionService.getDist(distId));
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:add')")
    @Log(title = "分发配置", businessType = BusinessType.INSERT)
    @PostMapping("/config")
    public AjaxResult addDist(@RequestBody MdmDistribution mdmDistribution)
    {
        return toAjax(distributionService.addDist(mdmDistribution));
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:edit')")
    @Log(title = "分发配置", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult editDist(@RequestBody MdmDistribution mdmDistribution)
    {
        return toAjax(distributionService.editDist(mdmDistribution));
    }

    @PreAuthorize("@ss.hasPermi('mdm:distribution:remove')")
    @Log(title = "分发配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/config/{distIds}")
    public AjaxResult deleteDists(@PathVariable Long[] distIds)
    {
        return toAjax(distributionService.deleteDists(distIds));
    }

    // ===== 分发记录 =====

    @PreAuthorize("@ss.hasPermi('mdm:distribution:list')")
    @GetMapping("/record/list")
    public TableDataInfo listRecord(MdmDistributionRecord mdmDistributionRecord)
    {
        startPage();
        List<MdmDistributionRecord> list = distributionService.listRecord(mdmDistributionRecord);
        return getDataTable(list);
    }

    /** 失败重推 */
    @PreAuthorize("@ss.hasPermi('mdm:distribution:edit')")
    @Log(title = "分发记录重推", businessType = BusinessType.UPDATE)
    @PutMapping("/record/retry/{recordId}")
    public AjaxResult retryRecord(@PathVariable Long recordId)
    {
        return toAjax(distributionService.retryRecord(recordId));
    }

    /** 分发监控：MQ 通道状态（1.1.0） */
    @PreAuthorize("@ss.hasPermi('mdm:distribution:list')")
    @GetMapping("/monitor")
    public AjaxResult monitor()
    {
        return success(distributionService.getMqMonitorData());
    }
}