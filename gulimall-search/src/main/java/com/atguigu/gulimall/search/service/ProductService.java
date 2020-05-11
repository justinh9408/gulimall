package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    boolean saveProductUp(List<SkuEsModel> models) throws IOException;
}
