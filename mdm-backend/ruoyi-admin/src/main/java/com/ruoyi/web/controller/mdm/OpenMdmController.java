package com.ruoyi.web.controller.mdm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.integration.domain.MdmApp;
import com.ruoyi.mdm.integration.service.IIntegrationService;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;

/**
 * 主数据对外接口（供订阅方系统机器调用）
 *
 * <p>鉴权：请求头 X-Appid / X-Timestamp / X-Sign，签名算法 HMAC-SHA256，
 * 原文 = appid|timestamp|body（GET 不带 body 为空串），密钥为应用 secret，时间窗口 5 分钟。
 * 无效凭证返回 HTTP 401。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/open/mdm")
public class OpenMdmController extends BaseController
{
    /** 时间窗口：5 分钟 */
    private static final long WINDOW_MS = 5 * 60 * 1000L;

    @Autowired
    private IIntegrationService integrationService;

    @Autowired
    private IMdmDataService dataService;

    /**
     * 主数据查询（按对象编码 + 属性条件）
     */
    @Anonymous
    @GetMapping("/data")
    public AjaxResult query(@RequestParam String objectCode, HttpServletRequest request, HttpServletResponse response)
    {
        AjaxResult auth = checkSign(request, null, response);
        if (auth != null)
        {
            return auth;
        }
        Map<String, Object> query = new HashMap<>();
        int pageNum = 1;
        int pageSize = 100;
        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet())
        {
            String k = e.getKey();
            String v = e.getValue().length > 0 ? e.getValue()[0] : null;
            if ("objectCode".equals(k) || StringUtils.isEmpty(v))
            {
                continue;
            }
            if ("pageNum".equals(k))
            {
                pageNum = Integer.parseInt(v);
            }
            else if ("pageSize".equals(k))
            {
                pageSize = Integer.parseInt(v);
            }
            else
            {
                query.put(k, v);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", dataService.countData(objectCode, query));
        result.put("rows", dataService.selectDataList(objectCode, query, pageNum, pageSize));
        return AjaxResult.success(result);
    }

    /**
     * 变更推送确认回执
     */
    @Anonymous
    @PostMapping("/distribution/confirm")
    public AjaxResult confirm(@RequestBody String rawBody, HttpServletRequest request, HttpServletResponse response)
    {
        AjaxResult auth = checkSign(request, rawBody, response);
        if (auth != null)
        {
            return auth;
        }
        Long recordId = JSON.parseObject(rawBody).getLong("recordId");
        if (recordId == null)
        {
            return error("recordId 不能为空");
        }
        return toAjax(integrationService.confirmRecord(recordId));
    }

    /**
     * 校验凭证与签名，非法返回 401 响应（非空即失败）
     */
    private AjaxResult checkSign(HttpServletRequest request, String body, HttpServletResponse response)
    {
        String appid = request.getHeader("X-Appid");
        String ts = request.getHeader("X-Timestamp");
        String sign = request.getHeader("X-Sign");
        if (StringUtils.isEmpty(appid) || StringUtils.isEmpty(ts) || StringUtils.isEmpty(sign))
        {
            return unauthorized(response);
        }
        MdmApp app = integrationService.getAppByAppid(appid);
        if (app == null || !"1".equals(app.getEnabled()))
        {
            return unauthorized(response);
        }
        long timestamp;
        try
        {
            timestamp = Long.parseLong(ts);
        }
        catch (NumberFormatException e)
        {
            return unauthorized(response);
        }
        // 防重放：时间窗口
        if (Math.abs(System.currentTimeMillis() - timestamp) > WINDOW_MS)
        {
            return unauthorized(response);
        }
        String target = appid + "|" + ts + "|" + (body == null ? "" : body);
        String expect = hmacSha256Hex(app.getSecret(), target);
        if (!MessageDigest.isEqual(expect.getBytes(StandardCharsets.UTF_8), sign.getBytes(StandardCharsets.UTF_8)))
        {
            return unauthorized(response);
        }
        return null;
    }

    private AjaxResult unauthorized(HttpServletResponse response)
    {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new AjaxResult(HttpServletResponse.SC_UNAUTHORIZED, "无效的凭证或签名");
    }

    private String hmacSha256Hex(String secret, String target)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(target.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("签名计算失败", e);
        }
    }
}