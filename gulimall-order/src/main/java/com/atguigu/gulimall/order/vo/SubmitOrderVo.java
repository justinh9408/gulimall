package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Justin
 */
@Data
public class SubmitOrderVo {

    private Long addrId;

    private Integer payMethod;

    private BigDecimal payPrice;

    private String note;

    private String orderToken;
}
