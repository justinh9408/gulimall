package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResultVo;

public interface MallSearchService {

    SearchResultVo search(SearchParamVo param);

}
