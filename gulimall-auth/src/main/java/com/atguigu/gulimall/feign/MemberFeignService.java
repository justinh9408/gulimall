package com.atguigu.gulimall.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.vo.LoginVo;
import com.atguigu.gulimall.vo.OAuthWeiboResponse;
import com.atguigu.gulimall.vo.RegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpSession;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody RegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody LoginVo vo);

    @PostMapping("/member/member/socialLogin")
    R socialUserLogin(@RequestBody OAuthWeiboResponse vo);
}
