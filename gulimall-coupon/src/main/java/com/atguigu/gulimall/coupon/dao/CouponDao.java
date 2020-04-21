package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 16:39:58
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
