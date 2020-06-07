package com.atguigu.gulimall.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.service.SeckillService;
import com.atguigu.gulimall.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: Justin
 */
@RestController
public class SeckKillSkuController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SecKillSkuRedisTo> tos = seckillService.getCurrentSeckillSkus();

        return R.ok().put("skus",tos);
    }

    @GetMapping("/seckill/sku/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo seckillSkuInfo = seckillService.seckillSkuInfo(skuId);

        return R.ok().put("secKillSku", seckillSkuInfo);
    }

    @GetMapping("/kill")
    public R kill(@RequestParam("num")Long num,
                  @RequestParam("killId")String killId,
                  @RequestParam("token")String token) {
        String orderSn = seckillService.kill(killId, token, num);

        return R.ok().put("orderSn",orderSn);
    }
}
