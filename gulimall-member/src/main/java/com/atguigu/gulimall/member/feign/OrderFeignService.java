package com.atguigu.gulimall.member.feign;

import com.atguigu.common.vo.OrderWithItemsVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @PostMapping("/order/order/ordersWithItems")
    List<OrderWithItemsVo> findOrdersWithItems(@RequestBody Map<String, Object> map);
}
