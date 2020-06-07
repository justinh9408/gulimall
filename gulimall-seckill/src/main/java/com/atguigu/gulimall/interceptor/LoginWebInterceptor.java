package com.atguigu.gulimall.interceptor;

import com.atguigu.common.constant.AuthConstant;
import com.atguigu.common.vo.OauthMember;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Justin
 */
@Component
public class LoginWebInterceptor implements HandlerInterceptor {

    public static ThreadLocal<OauthMember> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        OauthMember user = (OauthMember) request.getSession().getAttribute(AuthConstant.LOGIN_USER);
        if (user != null) {
            loginUser.set(user);
            return true;
        } else {
//            没有登录
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
