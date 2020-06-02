package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.SkuStockTo;
import com.atguigu.gulimall.ware.vo.LockOrderItemsVo;
import com.atguigu.gulimall.ware.vo.LockResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 17:08:09
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuStockTo> getSkusHaveStock(List<Long> skuIds);

    Boolean lockStock(LockOrderItemsVo vo);
}

