package com.ruoyi.mdm.maintenance.service;

import java.util.List;
import java.util.Map;

/**
 * 主数据动态数据 服务层
 *
 * @author ruoyi
 */
public interface IMdmDataService
{
    /**
     * 查询主数据列表（动态列，手动分页）
     *
     * @param objectCode 对象编码
     * @param query 查询条件（键为属性编码）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 数据列表
     */
    public List<Map<String, Object>> selectDataList(String objectCode, Map<String, Object> query, int pageNum, int pageSize);

    /**
     * 统计主数据条数
     *
     * @param objectCode 对象编码
     * @param query 查询条件
     * @return 条数
     */
    public long countData(String objectCode, Map<String, Object> query);

    /**
     * 查询主数据详情
     *
     * @param objectCode 对象编码
     * @param id 数据ID
     * @return 数据
     */
    public Map<String, Object> selectDataById(String objectCode, Long id);

    /**
     * 新增主数据
     *
     * @param objectCode 对象编码
     * @param data 数据（键为属性编码）
     * @return 结果
     */
    public int insertData(String objectCode, Map<String, Object> data);

    /**
     * 修改主数据
     *
     * @param objectCode 对象编码
     * @param id 数据ID
     * @param data 数据
     * @return 结果
     */
    public int updateData(String objectCode, Long id, Map<String, Object> data);

    /**
     * 删除主数据
     *
     * @param objectCode 对象编码
     * @param ids 数据ID数组
     * @return 结果
     */
    public int deleteDataByIds(String objectCode, Long[] ids);
}
