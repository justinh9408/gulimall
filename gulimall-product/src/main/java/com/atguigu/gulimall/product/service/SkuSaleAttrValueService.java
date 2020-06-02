package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SkuSaleAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 14:58:40
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuSaleAttrVo> findAllAttrValuesInSpu(Long spuId);

    List<String> getAttrAsStringList(Long skuId);
}

