package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: Justin
 */
@Controller
public class IndexController {

    @GetMapping("/{page}.html")
    public String indexPage(@PathVariable("page")String page) {

        return page;
    }
}
