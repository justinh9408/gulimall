package com.atguigu.gulimall.order.feign;

import com.atguigu.common.to.SkuStockTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.LockOrderItemsVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @GetMapping("/ware/waresku/hasstock")
    List<SkuStockTo> getSkusHaveStock(@RequestBody List<Long> skuIds);

    @PostMapping("/ware/waresku/lockStock")
    R lockStock(@RequestBody LockOrderItemsVo vo);
}
