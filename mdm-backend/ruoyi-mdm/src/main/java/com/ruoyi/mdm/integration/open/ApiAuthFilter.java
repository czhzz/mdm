package com.ruoyi.mdm.integration.open;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.integration.domain.MdmApp;
import com.ruoyi.mdm.integration.service.IIntegrationService;

/**
 * 集成对外接口鉴权（/open/integration/**）
 *
 * <p>校验 Header X-App-Id / X-App-Secret（mdm_app 凭证），失败返回 401。
 * 通过后把应用编码写入 request attribute，供 Open*Controller 使用。
 *
 * @author ruoyi
 */
@Component
public class ApiAuthFilter extends OncePerRequestFilter
{
    /** 鉴权通过后写入 request 的应用编码（appid） */
    public static final String ATTR_APP_CODE = "mdmAppCode";

    @Autowired
    private IIntegrationService integrationService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
    {
        return !request.getRequestURI().startsWith("/open/integration/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String appId = request.getHeader("X-App-Id");
        String secret = request.getHeader("X-App-Secret");
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret))
        {
            unauthorized(response);
            return;
        }
        MdmApp app = integrationService.getAppByAppid(appId);
        if (app == null || !"1".equals(app.getEnabled()) || !secret.equals(app.getSecret()))
        {
            unauthorized(response);
            return;
        }
        request.setAttribute(ATTR_APP_CODE, app.getAppid());
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"无效的应用凭证\"}");
    }
}
