package com.atguigu.gulimall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author: Justin
 */
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
//@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
public class GuliCartApp30000 {

    public static void main(String[] args) {

        SpringApplication.run(GuliCartApp30000.class, args);
    }
}

