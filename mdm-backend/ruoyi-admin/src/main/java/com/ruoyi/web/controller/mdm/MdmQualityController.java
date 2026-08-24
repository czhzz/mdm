package com.ruoyi.web.controller.mdm;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.ruoyi.mdm.quality.domain.MdmQualityIssue;
import com.ruoyi.mdm.quality.service.IMdmQualityService;

/**
 * 数据质量 信息操作处理（台账 + 重复检测）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/quality")
public class MdmQualityController extends BaseController
{
    @Autowired
    private IMdmQualityService qualityService;

    /**
     * 查询质量台账列表
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:list')")
    @GetMapping("/issue/list")
    public TableDataInfo list(MdmQualityIssue mdmQualityIssue)
    {
        startPage();
        List<MdmQualityIssue> list = qualityService.selectIssueList(mdmQualityIssue);
        return getDataTable(list);
    }

    /**
     * 处理质量问题（已处理/忽略）
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:edit')")
    @Log(title = "质量问题处理", businessType = BusinessType.UPDATE)
    @PutMapping("/issue/handle")
    public AjaxResult handle(@RequestBody MdmQualityIssue mdmQualityIssue)
    {
        return toAjax(qualityService.handleIssue(mdmQualityIssue));
    }

    /**
     * 重复检测（按字段分组，自动登记台账）
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:query')")
    @Log(title = "重复检测", businessType = BusinessType.OTHER)
    @PostMapping("/duplicate")
    public AjaxResult duplicate(@RequestBody Map<String, Object> body)
    {
        String objectCode = String.valueOf(body.getOrDefault("objectCode", ""));
        List<String> fields = (List<String>) body.get("fields");
        return success(qualityService.duplicateCheck(objectCode, fields));
    }

    /**
     * 质量大屏聚合数据（1.1.0）
     */
    @PreAuthorize("@ss.hasPermi('mdm:quality:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(qualityService.getDashboardData());
    }
}
