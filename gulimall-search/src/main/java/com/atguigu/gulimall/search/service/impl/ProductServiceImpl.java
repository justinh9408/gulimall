package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GuliElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author: Justin
 */
@Slf4j
@Service("productService")
public class ProductServiceImpl implements ProductService {

    @Autowired
    RestHighLevelClient esClient;

    @Override
    public boolean saveProductUp(List<SkuEsModel> models) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : models) {
            IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
            request.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            request.source(s, XContentType.JSON);

            bulkRequest.add(request);
        }

        BulkResponse bulk = esClient.bulk(bulkRequest, GuliElasticSearchConfig.COMMON_OPTIONS);

        boolean b = !bulk.hasFailures();

        return b;

    }
}
