package com.atguigu.gulimall.controller;

import com.atguigu.common.constant.AuthConstant;
import com.atguigu.gulimall.interceptor.UserStatusInterceptor;
import com.atguigu.gulimall.to.UserStatusTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * @author: Justin
 */
@Controller
public class CartController {

    @GetMapping("/cart.html")
    public String cartPage(HttpSession session) {
        UserStatusTo status = UserStatusInterceptor.userStatus.get();
        System.out.println(status);

        return "cartList";
    }
}
