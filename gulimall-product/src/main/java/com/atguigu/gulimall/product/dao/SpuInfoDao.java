package com.atguigu.gulimall.product.dao;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 14:58:41
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code")int code);
}
