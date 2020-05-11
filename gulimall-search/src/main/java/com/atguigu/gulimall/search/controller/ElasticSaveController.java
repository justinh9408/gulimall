package com.atguigu.gulimall.search.controller;

import com.atguigu.common.exception.ExceptionCode;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author: Justin
 */
@Slf4j
@RestController
@RequestMapping("/elastic/save")
public class ElasticSaveController {

    @Autowired
    ProductService productService;

    @PostMapping("/product")
    public R saveProductUp(@RequestBody List<SkuEsModel> models) {

        boolean b = false;
        try {
            b = productService.saveProductUp(models);
        } catch (IOException e) {
            log.error("货物上架错误:{}", e.getMessage());
            return R.error(ExceptionCode.PRODUCT_UP_EXCEPTION.getCode(), ExceptionCode.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if (b) {
            return R.ok();
        } else {
            return R.error(ExceptionCode.PRODUCT_UP_EXCEPTION.getCode(), ExceptionCode.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
