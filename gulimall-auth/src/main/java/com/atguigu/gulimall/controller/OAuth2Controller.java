package com.atguigu.gulimall.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.constant.AuthConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.feign.MemberFeignService;
import com.atguigu.gulimall.utils.HttpUtil;
import com.atguigu.gulimall.vo.OAuthWeiboResponse;
import com.atguigu.common.vo.OauthMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Justin
 */
@Controller
public class OAuth2Controller {


    @Autowired
    MemberFeignService memberFeignService;

    @Value("${gulimall.oauth2.appId}")
    String appId;


    @Value("${gulimall.oauth2.appSecret}")
    String appSecret;

    @GetMapping("/oauth2.0/weibo/success")
    public String accessToken(@RequestParam("code") String code, HttpSession httpSession) throws IOException {

//       "https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE";
        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", appId);
        params.put("client_secret", appSecret);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        params.put("code", code);
        OAuthWeiboResponse oAuthWeiboResponse = null;
        try {
            String res = HttpUtil.postForm("https://api.weibo.com/oauth2/access_token", params);
            System.out.println(res);
            oAuthWeiboResponse = JSON.parseObject(res, OAuthWeiboResponse.class);
//          登录/注册social user账户
            R r = memberFeignService.socialUserLogin(oAuthWeiboResponse);
            if (r.getCode() == 0) {
                Object member = r.get(AuthConstant.LOGIN_USER);
                String memberString = JSON.toJSONString(member);
                OauthMember memberEntity = JSON.parseObject(memberString, OauthMember.class);
//a01c59ef-6e7a-4db1-94c1-7600f91e50fc
                httpSession.setAttribute(AuthConstant.LOGIN_USER,memberEntity);

                return "redirect:http://gulimall.com";

            } else {

                return "redirect:http://auth.gulimall.com/login.html";
            }

        } catch (Exception e) {
            e.printStackTrace();

            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
