package com.ruoyi.mdm.lineage.service;

import java.util.Map;

/**
 * 主数据血缘追踪 服务层
 *
 * @author ruoyi
 */
public interface IMdmLineageService
{
    /**
     * 查询数据血缘（来源 + 去向）
     *
     * @param objectCode 对象编码
     * @param dataId 数据ID
     * @return { objectCode, dataId, dataCode, source: {...}, targets: [...] }
     */
    Map<String, Object> getLineage(String objectCode, Long dataId);
}