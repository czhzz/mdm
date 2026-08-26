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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成管理-对外接收接口
 *
 * <p>POST /open/integration/receive/{apiCode}，Header: X-App-Id / X-App-Secret（ApiAuthFilter 鉴权）
 * Body: { dataCode, data:{...} }；同 dataCode 重复推送按 UPDATE 处理（幂等）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/open/integration/receive")
public class OpenReceiveController
{
    @Autowired
    private IIntegrationService integrationService;

    @Anonymous
    @PostMapping("/{apiCode}")
    public AjaxResult receive(@PathVariable String apiCode,
            @RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        String dataCode = body.get("dataCode") == null ? null : String.valueOf(body.get("dataCode"));
        if (StringUtils.isEmpty(dataCode))
        {
            return AjaxResult.error("dataCode 不能为空");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        if (data == null || data.isEmpty())
        {
            return AjaxResult.error("data 不能为空");
        }
        String appCode = (String) request.getAttribute(ApiAuthFilter.ATTR_APP_CODE);
        String ip = request.getRemoteAddr();
        Map<String, Object> result = integrationService.receive(apiCode, dataCode, data, appCode, ip);
        return AjaxResult.success(result);
    }
}
