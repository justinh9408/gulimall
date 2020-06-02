package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author: Justin
 */
@Data
public class SubmitOrderResponseVo {
    private Long code = 0L;

    private OrderEntity order;
}
