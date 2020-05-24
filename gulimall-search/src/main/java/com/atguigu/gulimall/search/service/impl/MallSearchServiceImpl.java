package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GuliElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.AttrFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrRespVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

/**
 * @author: Justin
 */
@Slf4j
@Service("mallSearchService")
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient esClient;

    @Autowired
    AttrFeignService attrFeignService;

    @Override
    public SearchResultVo search(SearchParamVo param) {
        SearchResultVo searchResultVo = null;
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            SearchResponse searchResponse = esClient.search(searchRequest, GuliElasticSearchConfig.COMMON_OPTIONS);
//           分析响应数据
            searchResultVo = buildSearchResponse(searchResponse, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResultVo;
    }

    private SearchResultVo buildSearchResponse(SearchResponse searchResponse, SearchParamVo param) {
        SearchResultVo result = new SearchResultVo();
        SearchHits responseHits = searchResponse.getHits();

        ArrayList<SkuEsModel> esModels = new ArrayList<>();
        if (responseHits != null && responseHits.getHits().length > 0) {
            for (SearchHit hit : responseHits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                esModels.add(skuEsModel);
            }
        }
        result.setProducts(esModels);

//      整合聚合信息
//      分类聚合
        ParsedLongTerms catalog_agg = searchResponse.getAggregations().get("catalog_agg");
        ArrayList<SearchResultVo.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResultVo.CatalogVo catalogVo = new SearchResultVo.CatalogVo();
            String catalogId = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(catalogId));

            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            Terms.Bucket bucket1 = catalog_name_agg.getBuckets().get(0);
            catalogVo.setCatalogName(bucket1.getKeyAsString());
            catalogVos.add(catalogVo);
        }
        result.setRelevantCatalogs(catalogVos);

//        品牌聚合
        ParsedLongTerms brand_agg = searchResponse.getAggregations().get("brand_agg");
        ArrayList<SearchResultVo.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResultVo.BrandVo brandVo = new SearchResultVo.BrandVo();

            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandNmae = brand_name_agg.getBuckets().get(0).getKeyAsString();

            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();

            brandVo.setBrandName(brandNmae);
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);

        }
        result.setRelevantBrands(brandVos);


//        属性聚合
        ArrayList<SearchResultVo.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResultVo.AttrVo attrVo = new SearchResultVo.AttrVo();
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());

            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());

            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrValues(attrValues);
            attrVos.add(attrVo);
        }
        result.setRelevantAttrs(attrVos);

//        分页信息
        long ttr = responseHits.getTotalHits().value;
        Long ttp = ttr % EsConstant.PRODUCT_PAGESIZE == 0 ? ttr / EsConstant.PRODUCT_PAGESIZE : (ttr / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setPageNum(param.getPageNum().longValue());
        result.setTotalRows(ttr);
        result.setTotalPages(ttp);

        ArrayList<Long> pageNavs = new ArrayList<>();
        for (Long i = 1l; i <= ttp; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

//        构建面包屑导航属性vo
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResultVo.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);

//                远程调用获取attr名字
                R r = attrFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    Object data = r.get("attr");
                    String s1 = JSON.toJSONString(data);
                    AttrRespVo attrRespVo = JSON.parseObject(s1, AttrRespVo.class);
                    navVo.setNavName(attrRespVo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                String replace = replaceParamString(param.get_queryString(), attr,"attrs");
                param.set_queryString(replace);
//                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                navVo.setLink("http://localhost:12000/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavLine(navVos);
        }

        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResultVo.NavVo> navs = result.getNavLine();
            SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
            navVo.setNavName("品牌");
            R r = attrFeignService.brandinfos(param.getBrandId());
            if (r.getCode() == 0) {
                Object data = r.get("brands");
                String jsonString = JSON.toJSONString(data);
                List<BrandVo> brands = JSON.parseObject(jsonString, new TypeReference<List<BrandVo>>() {
                });
                StringBuffer stringBuffer = new StringBuffer();
                String s = param.get_queryString();
                for (BrandVo brandVo : brands) {
                    stringBuffer.append(brandVo.getName());
                    s = replaceParamString(s, brandVo.getBrandId().toString(), "brandId");
                }
                param.set_queryString(s);
                navVo.setNavValue(stringBuffer.toString());
                navVo.setLink("http://localhost:12000/list.html?"+s);
                result.getNavLine().add(navVo);
            }
            navs.add(navVo);
        }

//        ExecutorService executorService = Executors.newFixedThreadPool(5);
        return result;
    }

    private String replaceParamString(String s, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s.replace(key+"=" + encode, "");
    }

    private SearchRequest buildSearchRequest(SearchParamVo param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(param.getKeyWord())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyWord()));
        }

        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                } else {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        sourceBuilder.query(boolQuery);

//        排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], sortOrder);
        }

//        分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

//        聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(20);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(2);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        System.out.println("DSL: " + sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }


}
