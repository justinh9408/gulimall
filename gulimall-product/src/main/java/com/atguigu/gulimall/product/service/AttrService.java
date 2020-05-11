package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 14:58:40
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getAttrByGroupId(Long attrgroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId);

    List<AttrEntity> findAllAttr(Long groupId);

    List<Long> findSearchableAttrIds(List<Long> baseAttrsIds);
}

