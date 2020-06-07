package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.feign.SecKillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.ItemInfoVo;
import com.atguigu.gulimall.product.vo.SecKillSkuVo;
import com.atguigu.gulimall.product.vo.SkuSaleAttrVo;
import com.atguigu.gulimall.product.vo.SpuAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SecKillFeignService secKillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String  key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catelog_id", catelogId);
        }

        String brandId = (String) params.get("key");
        if (!StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(key)) {
            wrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(key)) {
            BigDecimal bigDecimal = null;
            try {
                bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal(0)) == 1) {
                    wrapper.le("price", max);
                }
            } catch (Exception e) {

            }
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        return skuInfoEntities;

    }

    @Override
    public ItemInfoVo item(Long skuId) throws ExecutionException, InterruptedException {
        ItemInfoVo infoVo = new ItemInfoVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = baseMapper.selectById(skuId);
            infoVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getBySkuId(skuId);
            infoVo.setImages(images);
        });

        CompletableFuture<Void> spuDesFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            infoVo.setSpuInfo(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SpuAttrGroupVo> spuAttrGroups = attrGroupService.findAttrGroupWithAttrsWithSpuId(res.getSpuId(), res.getCatalogId());
            infoVo.setSpuAttrGroups(spuAttrGroups);
        }, executor);

        CompletableFuture<Void> allAttrInSpuFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuSaleAttrVo> saleAttrVos = skuSaleAttrValueService.findAllAttrValuesInSpu(res.getSpuId());
            infoVo.setSaleAttrs(saleAttrVos);
        }, executor);

//        查找秒杀信息
        CompletableFuture<Void> secKillFuture = infoFuture.thenAcceptAsync((res) -> {
            R secKillR = secKillFeignService.getSeckillSkuInfo(skuId);
            if (secKillR.getCode() == 0) {
                SecKillSkuVo sku = secKillR.getDataInType("secKillSku", SecKillSkuVo.class);
                infoVo.setSecKillSkuVo(sku);
                System.out.println("秒杀商品: "+sku);
            }
        }, executor);

        CompletableFuture.allOf(imageFuture, spuDesFuture, attrGroupFuture, allAttrInSpuFuture,secKillFuture).get();

        return infoVo;
    }

}