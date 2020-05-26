package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.ItemInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author: Justin
 */
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;
    
    @GetMapping("/{skuId}.html")
    String itemPage(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        ItemInfoVo itemInfoVo = skuInfoService.item(skuId);
        model.addAttribute("item", itemInfoVo);

        return "item";
    }
}
