package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: Justin
 */
@Data
public class SpuAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
