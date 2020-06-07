package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.vo.OrderWithItemsVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import com.atguigu.gulimall.order.vo.SubmitOrderVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 16:56:57
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(SubmitOrderVo vo);

    OrderEntity getByOrderSn(String orderSn);

    void releaseOrder(OrderEntity orderEntity);

    List<OrderWithItemsVo> findOrdersWithItems(Map<String, Object> map);

    void payed(String sn);

    void createSecKillOrder(SecKillOrderTo secKillOrder);
}

