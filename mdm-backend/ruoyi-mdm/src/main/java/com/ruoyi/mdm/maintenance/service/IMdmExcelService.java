package com.ruoyi.mdm.maintenance.service;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 主数据 Excel 导入导出（动态属性列）
 *
 * @author ruoyi
 */
public interface IMdmExcelService
{
    /** 下载导入模板（表头为属性名） */
    public void downloadTemplate(String objectCode, HttpServletResponse response);

    /** 批量导入（逐行校验，返回成功/失败统计与失败行明细） */
    public AjaxResult importExcel(String objectCode, MultipartFile file);

    /** 按查询条件导出 */
    public void exportExcel(String objectCode, Map<String, Object> query, HttpServletResponse response);
}