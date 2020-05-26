package com.atguigu.gulimall.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.feign.MemberFeignService;
import com.atguigu.gulimall.vo.LoginVo;
import com.atguigu.gulimall.vo.RegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Justin
 */
@Controller
public class LoginController {

    @Autowired
    MemberFeignService memberFeignService;

    @PostMapping("/register")
    public String register(@Valid RegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(field -> field.getField(), fieldError -> fieldError.getDefaultMessage()));
//重定向临时attribute
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/reg.html";
//            return "redirect:auth.gulimall.com/reg.html";
        }

//        TODO:验证短信验证码

//        调用远程服务，保存会员账号
        R r = memberFeignService.register(vo);
        if (r.getCode() == 0) {
            return "redirect:/login.html";
        } else {
            redirectAttributes.addFlashAttribute("regErrors", r.get("msg"));
            return "redirect:/reg.html";
        }

    }

    @PostMapping("/login")
    public String login(LoginVo vo) {
        R login = memberFeignService.login(vo);
        if (login.getCode() != 0) {
            return "redirect:auth.gulimall.com/reg.html";
        }

        return "redirect:http://gulimall.com";
    }
}
