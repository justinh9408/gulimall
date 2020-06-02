package com.atguigu.gulimall.interceptor;

import com.atguigu.common.constant.AuthConstant;
import com.atguigu.common.vo.OauthMember;
import com.atguigu.gulimall.to.UserStatusTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author: Justin
 */
@Component
public class UserStatusInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserStatusTo> userStatus = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        OauthMember login = (OauthMember) session.getAttribute(AuthConstant.LOGIN_USER);
        UserStatusTo userStatusTo = new UserStatusTo();
        if (login != null) {
//            已登陆
            userStatusTo.setUserId(login.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equalsIgnoreCase(AuthConstant.TEMP_USER_KEY)) {
                    userStatusTo.setUserKey(cookie.getValue());
                    userStatusTo.setTemp_user(true);
                }
            }
        }
        if (StringUtils.isEmpty(userStatusTo.getUserKey())) {
            String userKey = UUID.randomUUID().toString();
            userStatusTo.setUserKey(userKey);
        }
        userStatus.set(userStatusTo);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserStatusTo userStatusTo = userStatus.get();

//        首次临时创建临时用户cookie
        if (!userStatusTo.getTemp_user()){
            Cookie cookie = new Cookie(AuthConstant.TEMP_USER_KEY, userStatusTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(60*30);
            response.addCookie(cookie);
        }
    }
}
