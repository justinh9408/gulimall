package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @author: Justin
 */
@Data
public class StockLockTo {

    private Long id;

    private StockDetailLockTo stockLockDetail;
}
