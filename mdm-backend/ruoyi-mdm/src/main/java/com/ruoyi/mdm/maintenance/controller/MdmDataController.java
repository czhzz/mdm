package com.ruoyi.mdm.maintenance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mdm.audit.service.IMdmAuditFlowableService;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;
import com.ruoyi.mdm.maintenance.service.IMdmExcelService;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

/**
 * 主数据动态数据 信息操作处理（按对象编码动态生成 CRUD）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/data")
public class MdmDataController extends BaseController
{
    @Autowired
    private IMdmDataService dataService;

    @Autowired
    private IMdmAuditFlowableService auditFlowableService;

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private IMdmExcelService excelService;

    /**
     * 判断对象是否启用审核流程（1.1.0：改为 Flowable，通过 auditProcessKey 判断）
     */
    private boolean auditEnabled(String objectCode)
    {
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (object == null)
        {
            return false;
        }
        return com.ruoyi.common.utils.StringUtils.isNotEmpty(object.getAuditProcessKey());
    }

    /**
     * 查询主数据列表（手动分页，动态属性列为查询条件）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:list')")
    @GetMapping("/{objectCode}/list")
    public TableDataInfo list(@PathVariable String objectCode, @RequestParam Map<String, Object> params)
    {
        int pageNum = Integer.parseInt(String.valueOf(params.getOrDefault("pageNum", "1")));
        int pageSize = Integer.parseInt(String.valueOf(params.getOrDefault("pageSize", "10")));
        Map<String, Object> query = new HashMap<>(params);
        query.remove("pageNum");
        query.remove("pageSize");
        query.remove("orderByColumn");
        query.remove("isAsc");
        List<Map<String, Object>> list = dataService.selectDataList(objectCode, query, pageNum, pageSize);
        long total = dataService.countData(objectCode, query);
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setRows(list);
        dataTable.setTotal(total);
        return dataTable;
    }

    /**
     * 获取主数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:query')")
    @GetMapping("/{objectCode}/{id}")
    public AjaxResult getInfo(@PathVariable String objectCode, @PathVariable Long id)
    {
        return success(dataService.selectDataById(objectCode, id));
    }

    /**
     * 新增主数据
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:add')")
    @Log(title = "主数据", businessType = BusinessType.INSERT)
    @PostMapping("/{objectCode}")
    public AjaxResult add(@PathVariable String objectCode, @RequestBody Map<String, Object> data)
    {
        // 启用审核的对象：新增走提交审核（Flowable）
        if (auditEnabled(objectCode))
        {
            auditFlowableService.submitAudit(objectCode, 0L, "INSERT", data);
            return success("已提交审核，审核通过后生效");
        }
        return toAjax(dataService.insertData(objectCode, data));
    }

    /**
     * 修改主数据（启用审核时走待审核版本，不直接覆盖）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:edit')")
    @Log(title = "主数据", businessType = BusinessType.UPDATE)
    @PutMapping("/{objectCode}/{id}")
    public AjaxResult edit(@PathVariable String objectCode, @PathVariable Long id, @RequestBody Map<String, Object> data)
    {
        // 启用审核的对象：修改走提交审核（Flowable），原数据保持生效
        if (auditEnabled(objectCode))
        {
            auditFlowableService.submitAudit(objectCode, id, "UPDATE", data);
            return success("修改已提交审核，审核通过后生效");
        }
        return toAjax(dataService.updateData(objectCode, id, data));
    }

    /**
     * 删除主数据
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:remove')")
    @Log(title = "主数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{objectCode}/{ids}")
    public AjaxResult remove(@PathVariable String objectCode, @PathVariable Long[] ids)
    {
        return toAjax(dataService.deleteDataByIds(objectCode, ids));
    }

    /**
     * 下载导入模板（表头为属性名）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:query')")
    @GetMapping("/{objectCode}/template")
    public void template(@PathVariable String objectCode, HttpServletResponse response)
    {
        excelService.downloadTemplate(objectCode, response);
    }

    /**
     * 批量导入（逐行校验 + 失败行反馈）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:add')")
    @Log(title = "主数据导入", businessType = BusinessType.IMPORT)
    @PostMapping("/{objectCode}/import")
    public AjaxResult importExcel(@PathVariable String objectCode, @RequestParam("file") MultipartFile file)
    {
        return excelService.importExcel(objectCode, file);
    }

    /**
     * 按查询条件导出（动态属性列）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:query')")
    @GetMapping("/{objectCode}/export")
    public void export(@PathVariable String objectCode, @RequestParam Map<String, Object> params,
            HttpServletResponse response)
    {
        Map<String, Object> query = new HashMap<>(params);
        query.remove("pageNum");
        query.remove("pageSize");
        query.remove("orderByColumn");
        query.remove("isAsc");
        excelService.exportExcel(objectCode, query, response);
    }

    /**
     * 查询引用数据列表（供前端下拉选择）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:query')")
    @GetMapping("/ref/{objectCode}")
    public AjaxResult refData(@PathVariable String objectCode,
            @RequestParam(required = false) String display,
            @RequestParam(required = false) String keyword)
    {
        return success(dataService.selectRefDataList(objectCode, display, keyword));
    }

    /**
     * 更新主数据生命周期状态（生效/停用）
     */
    @PreAuthorize("@ss.hasPermi('mdm:maintenance:edit')")
    @Log(title = "主数据状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{objectCode}/{id}/status")
    public AjaxResult updateStatus(@PathVariable String objectCode, @PathVariable Long id, @RequestBody Map<String, Object> body)
    {
        String status = String.valueOf(body.getOrDefault("status", ""));
        return toAjax(dataService.updateDataStatus(objectCode, id, status));
    }
}
