package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: Justin
 */
@Data
public class LockOrderItemsVo {

    private String orderSn;

    private List<OrderItemVo> lockedItems;
}
