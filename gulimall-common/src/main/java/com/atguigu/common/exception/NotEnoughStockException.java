package com.atguigu.common.exception;

/**
 * @author: Justin
 */
public class NotEnoughStockException extends RuntimeException {

    public NotEnoughStockException(Long skuId) {
        super(skuId+"号商品库存不足");
    }
}
