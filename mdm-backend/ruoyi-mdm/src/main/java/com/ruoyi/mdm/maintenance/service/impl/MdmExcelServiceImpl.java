package com.ruoyi.mdm.maintenance.service.impl;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;
import com.ruoyi.mdm.maintenance.service.IMdmExcelService;
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmAttributeMapper;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

/**
 * 主数据 Excel 导入导出实现（POI 直写动态属性列）
 *
 * @author ruoyi
 */
@Service
public class MdmExcelServiceImpl implements IMdmExcelService
{
    private static final Logger log = LoggerFactory.getLogger(MdmExcelServiceImpl.class);

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private MdmAttributeMapper attributeMapper;

    @Autowired
    private IMdmDataService dataService;

    @Override
    public void downloadTemplate(String objectCode, HttpServletResponse response)
    {
        MdmObject object = getObject(objectCode);
        List<MdmAttribute> attrs = attrs(object.getObjectId());
        try (Workbook wb = new XSSFWorkbook())
        {
            Sheet sheet = wb.createSheet("导入模板");
            Row header = sheet.createRow(0);
            for (int i = 0; i < attrs.size(); i++)
            {
                header.createCell(i).setCellValue(attrs.get(i).getAttrName());
                sheet.setColumnWidth(i, 20 * 256);
            }
            write(wb, object.getObjectName() + "导入模板.xlsx", response);
        }
        catch (Exception e)
        {
            throw new ServiceException("模板生成失败：" + e.getMessage());
        }
    }

    @Override
    public AjaxResult importExcel(String objectCode, MultipartFile file)
    {
        MdmObject object = getObject(objectCode);
        List<MdmAttribute> attrs = attrs(object.getObjectId());
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请选择要导入的 Excel 文件");
        }
        int total = 0;
        int success = 0;
        List<Map<String, Object>> failures = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream()))
        {
            Sheet sheet = wb.getSheetAt(0);
            Row headRow = sheet.getRow(0);
            // 表头 -> 属性映射（校验列）
            Map<Integer, MdmAttribute> colMap = new LinkedHashMap<>();
            for (int i = 0; i < headRow.getLastCellNum(); i++)
            {
                String head = cellText(headRow.getCell(i));
                if (StringUtils.isEmpty(head))
                {
                    continue;
                }
                MdmAttribute match = attrs.stream()
                        .filter(a -> head.equals(a.getAttrName())).findFirst().orElse(null);
                if (match == null)
                {
                    throw new ServiceException("模板列「" + head + "」与对象属性不匹配，请下载最新模板");
                }
                colMap.put(i, match);
            }
            if (colMap.isEmpty())
            {
                throw new ServiceException("模板表头为空");
            }
            DataFormatter formatter = new DataFormatter();
            for (int r = 1; r <= sheet.getLastRowNum(); r++)
            {
                Row row = sheet.getRow(r);
                if (row == null || allBlank(row, colMap.keySet()))
                {
                    continue;
                }
                total++;
                Map<String, Object> data = new LinkedHashMap<>();
                for (Map.Entry<Integer, MdmAttribute> e : colMap.entrySet())
                {
                    String val = formatter.formatCellValue(row.getCell(e.getKey())).trim();
                    if (StringUtils.isNotEmpty(val))
                    {
                        data.put(e.getValue().getAttrCode(), val);
                    }
                }
                try
                {
                    dataService.insertData(objectCode, data);
                    success++;
                }
                catch (Exception ex)
                {
                    Map<String, Object> fail = new LinkedHashMap<>();
                    fail.put("row", r + 1);
                    fail.put("error", ex.getMessage());
                    failures.add(fail);
                }
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("Excel导入失败", e);
            throw new ServiceException("导入文件解析失败：" + e.getMessage());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("failures", failures);
        return AjaxResult.success("导入完成：共 " + total + " 条，成功 " + success + " 条，失败 " + failures.size() + " 条", result);
    }

    @Override
    public void exportExcel(String objectCode, Map<String, Object> query, HttpServletResponse response)
    {
        MdmObject object = getObject(objectCode);
        List<MdmAttribute> attrs = attrs(object.getObjectId());
        // 导出全部（演示规模下按大分页拉取）
        List<Map<String, Object>> rows = dataService.selectDataList(objectCode, query, 1, 10000);
        try (Workbook wb = new XSSFWorkbook())
        {
            Sheet sheet = wb.createSheet("主数据");
            Row header = sheet.createRow(0);
            for (int i = 0; i < attrs.size(); i++)
            {
                header.createCell(i).setCellValue(attrs.get(i).getAttrName());
                sheet.setColumnWidth(i, 20 * 256);
            }
            int r = 1;
            for (Map<String, Object> rowMap : rows)
            {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < attrs.size(); c++)
                {
                    Object v = rowMap.get(attrs.get(c).getAttrCode());
                    if (v != null)
                    {
                        row.createCell(c).setCellValue(String.valueOf(v));
                    }
                }
            }
            write(wb, object.getObjectName() + ".xlsx", response);
        }
        catch (Exception e)
        {
            throw new ServiceException("导出失败：" + e.getMessage());
        }
    }

    private MdmObject getObject(String objectCode)
    {
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (object == null)
        {
            throw new ServiceException("对象不存在：" + objectCode);
        }
        return object;
    }

    private List<MdmAttribute> attrs(Long objectId)
    {
        MdmAttribute query = new MdmAttribute();
        query.setObjectId(objectId);
        return attributeMapper.selectAttributeList(query);
    }

    private String cellText(Cell cell)
    {
        return cell == null ? "" : new DataFormatter().formatCellValue(cell).trim();
    }

    private boolean allBlank(Row row, java.util.Set<Integer> cols)
    {
        for (Integer c : cols)
        {
            if (StringUtils.isNotEmpty(cellText(row.getCell(c))))
            {
                return false;
            }
        }
        return true;
    }

    private void write(Workbook wb, String fileName, HttpServletResponse response) throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        String encoded = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + encoded);
        response.getOutputStream().write(out.toByteArray());
        response.getOutputStream().flush();
    }
}