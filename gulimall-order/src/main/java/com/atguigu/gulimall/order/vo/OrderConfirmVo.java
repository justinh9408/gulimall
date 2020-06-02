package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author: Justin
 */
public class OrderConfirmVo {

    @Getter @Setter
    private List<ReceiverAddrVo> address;

    @Getter @Setter
    private List<OrderItemVo> items;

    @Getter @Setter
    private Integer integration;

    @Getter @Setter
    private String orderToken; //防止重复提交

    @Getter @Setter
    private Map<Long,Boolean> stocks;

//    private BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0.00");
        for (OrderItemVo orderItemVo : items) {
            sum = sum.add(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())));
        }
        return sum;
    }

//    private BigDecimal payAmount;

    public BigDecimal getPayAmount() {
        return getTotal();
    }
}
