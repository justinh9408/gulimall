package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: Justin
 */
@Component
@RabbitListener(queues = "order.secKill.queue")
public class SecKillOrderListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void createSecKillOrder(SecKillOrderTo secKillOrder, Message message, Channel channel) throws IOException {
        System.out.println("准备创建秒杀订单。。。。");
        try {
            orderService.createSecKillOrder(secKillOrder);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
