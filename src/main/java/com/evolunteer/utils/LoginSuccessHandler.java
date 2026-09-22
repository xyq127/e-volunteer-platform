package com.evolunteer.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 登录成功处理器：登录成功后按当前账号的角色跳转到对应的工作台，
 * 志愿者进入志愿者工作台，志愿者组织进入志愿者组织工作台，平台管理员进入平台管理员工作台。
 */
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 登录成功后的跳转处理
     *
     * @param request        请求对象
     * @param response       响应对象
     * @param authentication 登录认证信息
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String contextPath = request.getContextPath();

        if (roles.contains("ROLE_VOLUNTEER")) {
            response.sendRedirect(contextPath + "/volunteer/index.html");
        } else if (roles.contains("ROLE_ORGANIZATION")) {
            response.sendRedirect(contextPath + "/org/index.html");
        } else if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect(contextPath + "/admin/index.html");
        } else {
            response.sendRedirect(contextPath + "/portal/index.html");
        }
    }
}
