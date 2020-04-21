package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 16:56:57
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
