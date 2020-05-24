package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: Justin
 */
@Data
public class SearchParamVo {

    private Integer pageNum = 1;

    private String keyWord;

    private Long catalog3Id;

    private String sort;

    /**
     * 好多的过滤条件：
     * hasStock(是否有货) 0/1
     * 价格区间 skuPrice：1_500/_500/500_
     * 属性 attrs:2_5寸：6寸
     * brandId:1
     */
    private Integer hasStock = 1;

    private String skuPrice;

    private List<Long> brandId;

    private List<String> attrs;

    private String _queryString;


}
