package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.OrderWebInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: Justin
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    OrderWebInterceptor orderWebInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderWebInterceptor).addPathPatterns("/**");
    }
}
