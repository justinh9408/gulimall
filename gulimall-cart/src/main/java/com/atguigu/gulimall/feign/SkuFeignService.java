package com.atguigu.gulimall.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-product")
public interface SkuFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R skuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/attrString/{skuId}")
    List<String> skuAttrString(@PathVariable("skuId") Long skuId);
}
