package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.CatalogLv2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 14:58:41
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenusByIds(List<Long> asList);

    Long[] findCateLogIdPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> findLevel1Categories();

    Map<String, List<CatalogLv2Vo>> getCatalogJson();
}

