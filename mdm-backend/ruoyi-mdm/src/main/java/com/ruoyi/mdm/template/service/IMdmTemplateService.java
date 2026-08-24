package com.ruoyi.mdm.template.service;

import java.util.List;
import java.util.Map;

/**
 * 主数据对象模板Service接口
 *
 * @author ruoyi
 */
public interface IMdmTemplateService
{
    /**
     * 获取模板列表（简要信息）
     */
    List<Map<String, Object>> listTemplates();

    /**
     * 获取模板详情（含完整属性、编码方案等）
     */
    Map<String, Object> previewTemplate(String code);

    /**
     * 从模板一键创建对象
     */
    int createFromTemplate(String code);
}