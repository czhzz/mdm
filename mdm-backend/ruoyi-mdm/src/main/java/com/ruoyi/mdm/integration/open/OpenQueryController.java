package com.ruoyi.mdm.integration.open;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成管理-对外查询接口
 *
 * <p>POST /open/integration/query/{apiCode}，Header: X-App-Id / X-App-Secret（ApiAuthFilter 鉴权）
 * Body: { filters:{...}, pageNum, pageSize }，按对象查动态表返回主数据列表。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/open/integration/query")
public class OpenQueryController
{
    @Autowired
    private IIntegrationService integrationService;

    @Anonymous
    @PostMapping("/{apiCode}")
    public AjaxResult query(@PathVariable String apiCode,
            @RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) body.getOrDefault("filters", new java.util.HashMap<>());
        int pageNum = body.get("pageNum") == null ? 1 : Integer.parseInt(String.valueOf(body.get("pageNum")));
        int pageSize = body.get("pageSize") == null ? 10 : Integer.parseInt(String.valueOf(body.get("pageSize")));
        pageSize = Math.min(pageSize, 100);
        String appCode = (String) request.getAttribute(ApiAuthFilter.ATTR_APP_CODE);
        String ip = request.getRemoteAddr();
        Map<String, Object> result = integrationService.queryOpen(apiCode, filters, pageNum, pageSize, appCode, ip);
        return AjaxResult.success(result);
    }
}
