package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: Justin
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String img;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;
}
