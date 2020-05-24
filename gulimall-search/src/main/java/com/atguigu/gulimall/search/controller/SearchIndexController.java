package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: Justin
 */
@Controller
public class SearchIndexController {

    @Autowired
    MallSearchService searchService;


    @GetMapping(path = {"/list.html","/search.html"})
    public String searchIndexPage(SearchParamVo param, Model model, HttpServletRequest request) {
        param.set_queryString(request.getQueryString());
        SearchResultVo respVo = searchService.search(param);
        model.addAttribute("result", respVo);

        return "list";
    }
}
