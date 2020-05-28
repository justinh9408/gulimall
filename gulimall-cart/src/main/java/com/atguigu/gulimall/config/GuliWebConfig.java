package com.atguigu.gulimall.config;

import com.atguigu.gulimall.interceptor.UserStatusInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: Justin
 */
@Configuration
public class GuliWebConfig implements WebMvcConfigurer {

    @Autowired
    UserStatusInterceptor userStatusInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userStatusInterceptor).addPathPatterns("/**");
    }
}
