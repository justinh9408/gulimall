package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author: Justin
 */
@Data
public class LockResult {

    private Long skuId;
    private Integer num;
    private Boolean locked = false;
}
