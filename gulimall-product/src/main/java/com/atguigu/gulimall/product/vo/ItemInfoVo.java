package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: Justin
 */
@Data
public class ItemInfoVo {
    private SkuInfoEntity info;

    private Boolean hasStock = true;

    private List<SkuImagesEntity> images;

    private SpuInfoDescEntity spuInfo;

    private List<SkuSaleAttrVo> saleAttrs;

    private List<SpuAttrGroupVo> spuAttrGroups;

}
