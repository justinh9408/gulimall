package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Justin
 */
@Data
public class SearchResultVo {

    List<SkuEsModel> products;

//    分写信息
    private Long pageNum;
    private Long totalRows;
    private Long totalPages;
    private List<Long> pageNavs;

    private List<BrandVo> relevantBrands;
    private List<CatalogVo> relevantCatalogs;
    private List<AttrVo> relevantAttrs;

//    面包屑导航
    private List<NavVo> navLine = new ArrayList<>();

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;

        private List<String> attrValues;
    }
}
