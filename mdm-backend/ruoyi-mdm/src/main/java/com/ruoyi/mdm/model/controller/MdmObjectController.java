package com.ruoyi.mdm.model.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.service.IMdmAttributeService;
import com.ruoyi.mdm.model.service.IMdmObjectService;

/**
 * 主数据对象 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mdm/model/object")
public class MdmObjectController extends BaseController
{
    @Autowired
    private IMdmObjectService objectService;

    @Autowired
    private IMdmAttributeService attributeService;

    /**
     * 查询主数据对象列表
     */
    @PreAuthorize("@ss.hasPermi('mdm:model:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdmObject mdmObject)
    {
        startPage();
        List<MdmObject> list = objectService.selectObjectList(mdmObject);
        return getDataTable(list);
    }

    /**
     * 获取主数据对象详细信息
     */
    @PreAuthorize("@ss.hasPermi('mdm:model:query')")
    @GetMapping(value = "/{objectId}")
    public AjaxResult getInfo(@PathVariable Long objectId)
    {
        return success(objectService.selectObjectById(objectId));
    }

    /**
     * 新增主数据对象
     */
    @PreAuthorize("@ss.hasPermi('mdm:model:add')")
    @Log(title = "主数据对象", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MdmObject mdmObject)
    {
        if (!objectService.checkObjectCodeUnique(mdmObject))
        {
            return error("新增对象'" + mdmObject.getObjectName() + "'失败，对象编码已存在");
        }
        return toAjax(objectService.insertObject(mdmObject));
    }

    /**
     * 修改主数据对象
     */
    @PreAuthorize("@ss.hasPermi('mdm:model:edit')")
    @Log(title = "主数据对象", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MdmObject mdmObject)
    {
        return toAjax(objectService.updateObject(mdmObject));
    }

    /**
     * 获取对象模型元数据（对象 + 属性，供维护页动态渲染）
     */
    @PreAuthorize("@ss.hasPermi('mdm:model:query')")
    @GetMapping("/meta/{objectId}")
    public AjaxResult meta(@PathVariable Long objectId)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("object", objectService.selectObjectById(objectId));
        MdmAttribute query = new MdmAttribute();
        query.setObjectId(objectId);
        result.put("attributes", attributeService.selectAttributeList(query));
        return success(result);
    }

    /**
     * 发布主数据对象（动态建表）
     */
    @PreAuthorize("@ss.hasPermi('mdm:model:edit')")
    @Log(title = "主数据对象", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{objectId}")
    public AjaxResult publish(@PathVariable Long objectId)
    {
        return toAjax(objectService.publishObject(objectId));
    }

    /**
     * 删除主数据对象
     */
    @PreAuthorize("@ss.hasPermi('mdm:model:remove')")
    @Log(title = "主数据对象", businessType = BusinessType.DELETE)
    @DeleteMapping("/{objectIds}")
    public AjaxResult remove(@PathVariable Long[] objectIds)
    {
        return toAjax(objectService.deleteObjectByIds(objectIds));
    }
}
