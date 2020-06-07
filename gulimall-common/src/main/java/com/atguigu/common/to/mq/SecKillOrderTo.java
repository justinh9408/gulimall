package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Justin
 */
@Data
public class SecKillOrderTo {

    private String orderSn;

    private Long memberId;

    private Long promotionSessionId;

//  购买数量
    private Long num;

    private Long skuId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;


}
