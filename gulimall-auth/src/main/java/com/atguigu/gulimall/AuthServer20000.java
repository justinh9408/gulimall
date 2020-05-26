package com.atguigu.gulimall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Justin
 */
@EnableFeignClients("com.atguigu.gulimall.feign")
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServer20000 {

    public static void main(String[]  args) {
        SpringApplication.run(AuthServer20000.class, args);
    }
}
