package com.atguigu.gulimall.member.web;

import com.atguigu.common.vo.OrderWithItemsVo;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

/**
 * @author: Justin
 */
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/orderList")
    public String orderList(@RequestParam(value = "pageNum",defaultValue = "1")String pageNum, Model model) {
        HashMap<String , Object > map = new HashMap<>();
        map.put("page", pageNum);
        List<OrderWithItemsVo> orders = orderFeignService.findOrdersWithItems(map);

        model.addAttribute("orders", orders);
        return "orderList";
    }
}
