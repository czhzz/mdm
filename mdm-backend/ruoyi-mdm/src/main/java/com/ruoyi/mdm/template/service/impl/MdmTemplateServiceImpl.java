package com.ruoyi.mdm.template.service.impl;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmAttributeMapper;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;
import com.ruoyi.mdm.template.service.IMdmTemplateService;

/**
 * 主数据对象模板Service实现
 *
 * @author ruoyi
 */
@Service
public class MdmTemplateServiceImpl implements IMdmTemplateService
{
    /** 5 个预置模板定义 */
    private static final List<Map<String, Object>> TEMPLATES = Arrays.asList(
        templateDef("customer", "客户", "标准客户主数据模板，含分类、联系人、电话、地址", "客户", "customer",
            new String[][]{
                {"name", "客户名称", "text", "Y", "Y", "Y"},
                {"category", "客户分类", "dict", "Y", "N", "N"},
                {"contact", "联系人", "text", "N", "N", "N"},
                {"phone", "联系电话", "text", "N", "N", "N"},
                {"address", "地址", "text", "N", "N", "N"},
            },
            "KH", "客户编码", "DAY", new String[][]{{"CONSTANT", "KH", "1"}, {"DATE", "yyyyMMdd", "2"}, {"SEQUENCE", "4", "3"}}),
        templateDef("supplier", "供应商", "标准供应商主数据模板，含分类、联系人、电话、地址", "供应商", "supplier",
            new String[][]{
                {"name", "供应商名称", "text", "Y", "Y", "Y"},
                {"category", "供应商分类", "dict", "Y", "N", "N"},
                {"contact", "联系人", "text", "N", "N", "N"},
                {"phone", "联系电话", "text", "N", "N", "N"},
                {"address", "地址", "text", "N", "N", "N"},
            },
            "GYS", "供应商编码", "DAY", new String[][]{{"CONSTANT", "GYS", "1"}, {"DATE", "yyyyMMdd", "2"}, {"SEQUENCE", "4", "3"}}),
        templateDef("material", "物料", "标准物料主数据模板，含分类、名称、规格、单位", "物料", "material",
            new String[][]{
                {"name", "物料名称", "text", "Y", "Y", "Y"},
                {"category", "物料分类", "dict", "Y", "N", "N"},
                {"spec", "规格", "text", "N", "N", "N"},
                {"unit", "单位", "text", "N", "N", "N"},
            },
            "WL", "物料编码", "DAY", new String[][]{{"CONSTANT", "WL", "1"}, {"DATE", "yyyyMMdd", "2"}, {"SEQUENCE", "4", "3"}}),
        templateDef("organization", "组织", "标准组织主数据模板，含类型、名称、上级、负责人", "组织", "organization",
            new String[][]{
                {"name", "组织名称", "text", "Y", "Y", "Y"},
                {"type", "组织类型", "dict", "Y", "N", "N"},
                {"parent_code", "上级组织编码", "text", "N", "N", "N"},
                {"leader", "负责人", "text", "N", "N", "N"},
            },
            "ZZ", "组织编码", "DAY", new String[][]{{"CONSTANT", "ZZ", "1"}, {"DATE", "yyyyMMdd", "2"}, {"SEQUENCE", "4", "3"}}),
        templateDef("person", "人员", "标准人员主数据模板，含姓名、工号、部门、职位、手机", "人员", "person",
            new String[][]{
                {"name", "姓名", "text", "Y", "Y", "Y"},
                {"employee_no", "工号", "text", "Y", "Y", "N"},
                {"dept", "部门", "text", "N", "N", "N"},
                {"position", "职位", "dict", "N", "N", "N"},
                {"mobile", "手机", "text", "N", "N", "N"},
            },
            "RY", "人员编码", "DAY", new String[][]{{"CONSTANT", "RY", "1"}, {"DATE", "yyyyMMdd", "2"}, {"SEQUENCE", "4", "3"}})
    );

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private MdmAttributeMapper attributeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Map<String, Object> templateDef(String code, String name, String desc, String objName,
            String objCode, String[][] attrs, String codePrefix, String codeField, String resetType, String[][] segments)
    {
        Map<String, Object> tpl = new LinkedHashMap<>();
        tpl.put("code", code);
        tpl.put("name", name);
        tpl.put("description", desc);
        tpl.put("objectCode", objCode);
        tpl.put("objectName", objName);
        List<Map<String, String>> attrList = new ArrayList<>();
        for (String[] a : attrs)
        {
            Map<String, String> attr = new LinkedHashMap<>();
            attr.put("attrCode", a[0]);
            attr.put("attrName", a[1]);
            attr.put("dataType", a[2]);
            attr.put("requiredFlag", a[3]);
            attr.put("uniqueFlag", a[4]);
            attr.put("primaryFlag", a[5]);
            attrList.add(attr);
        }
        tpl.put("attributes", attrList);
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("ruleName", objName + "编码规则");
        rule.put("resetType", resetType);
        rule.put("codeField", codeField);
        List<Map<String, String>> segList = new ArrayList<>();
        for (String[] s : segments)
        {
            Map<String, String> seg = new LinkedHashMap<>();
            seg.put("segType", s[0]);
            seg.put("segValue", s[1]);
            seg.put("orderNum", s[2]);
            segList.add(seg);
        }
        rule.put("segments", segList);
        tpl.put("codeRule", rule);
        return tpl;
    }

    @Override
    public List<Map<String, Object>> listTemplates()
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> tpl : TEMPLATES)
        {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("code", tpl.get("code"));
            summary.put("name", tpl.get("name"));
            summary.put("description", tpl.get("description"));
            summary.put("objectCode", tpl.get("objectCode"));
            @SuppressWarnings("unchecked")
            List<Map<String, String>> attrs = (List<Map<String, String>>) tpl.get("attributes");
            summary.put("attributes", attrs);
            summary.put("codeRule", tpl.get("codeRule"));
            list.add(summary);
        }
        return list;
    }

    @Override
    public Map<String, Object> previewTemplate(String code)
    {
        for (Map<String, Object> tpl : TEMPLATES)
        {
            if (code.equals(tpl.get("code")))
            {
                return tpl;
            }
        }
        throw new ServiceException("模板不存在：" + code);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public int createFromTemplate(String code)
    {
        Map<String, Object> tpl = previewTemplate(code);
        String objCode = (String) tpl.get("objectCode");
        // 检查对象编码是否已存在
        MdmObject existing = objectMapper.checkObjectCodeUnique(objCode);
        if (existing != null)
        {
            throw new ServiceException("对象编码已存在：" + objCode);
        }
        // 1. 创建对象
        MdmObject obj = new MdmObject();
        obj.setObjectCode(objCode);
        obj.setObjectName((String) tpl.get("objectName"));
        obj.setTemplateSource(code);
        obj.setStatus("0");
        obj.setVersion("1.0");
        obj.setOrderNum(0);
        obj.setCreateBy(SecurityUtils.getUsername());
        objectMapper.insertObject(obj);

        // 2. 创建属性
        List<Map<String, String>> attrs = (List<Map<String, String>>) tpl.get("attributes");
        int order = 1;
        for (Map<String, String> a : attrs)
        {
            MdmAttribute attr = new MdmAttribute();
            attr.setObjectId(obj.getObjectId());
            attr.setAttrCode(a.get("attrCode"));
            attr.setAttrName(a.get("attrName"));
            attr.setDataType(a.get("dataType"));
            attr.setRequiredFlag(a.get("requiredFlag"));
            attr.setUniqueFlag(a.get("uniqueFlag"));
            attr.setPrimaryFlag(a.get("primaryFlag"));
            if ("dict".equals(a.get("dataType")))
            {
                attr.setSourceType("dict");
            }
            attr.setOrderNum(order++);
            attr.setCreateBy(SecurityUtils.getUsername());
            attributeMapper.insertAttribute(attr);
        }

        // 3. 创建编码方案
        Map<String, Object> rule = (Map<String, Object>) tpl.get("codeRule");
        if (rule != null)
        {
            jdbcTemplate.update(
                "INSERT INTO mdm_code_rule (object_id, rule_name, reset_type, code_field, status, create_by, create_time) VALUES (?, ?, ?, ?, '0', ?, sysdate())",
                obj.getObjectId(), rule.get("ruleName"), rule.get("resetType"), rule.get("codeField"), SecurityUtils.getUsername());
            Long ruleId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            List<Map<String, String>> segs = (List<Map<String, String>>) rule.get("segments");
            if (segs != null && ruleId != null)
            {
                for (Map<String, String> s : segs)
                {
                    jdbcTemplate.update(
                        "INSERT INTO mdm_code_rule_segment (rule_id, seg_type, seg_value, order_num) VALUES (?, ?, ?, ?)",
                        ruleId, s.get("segType"), s.get("segValue"), Integer.parseInt(s.get("orderNum")));
                }
            }
        }

        // 4. 注册菜单
        jdbcTemplate.update(
            "INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, visible, status, perms, icon, create_by, create_time) "
            + "SELECT COALESCE(MAX(menu_id), 2000) + 1, ?, 2000, 20, ?, 'mdm/maintenance/index', '0', '0', 'mdm:maintenance:list', 'example', ?, sysdate() FROM sys_menu",
            (String) tpl.get("objectName"), objCode, SecurityUtils.getUsername());

        return 1;
    }
}