package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.constant.AuthConstant;
import com.atguigu.common.exception.ExceptionCode;
import com.atguigu.common.vo.OauthMember;
import com.atguigu.gulimall.member.exception.ExistingPhoneNumException;
import com.atguigu.gulimall.member.exception.ExistingUserNameException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.servlet.http.HttpSession;


/**
 * 会员
 *
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 16:51:55
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {

        try {
            memberService.register(vo);
        } catch (ExistingUserNameException userNameException) {
            return R.error(ExceptionCode.USERNAME_EXIST_EXCEPTION.getCode(), ExceptionCode.USERNAME_EXIST_EXCEPTION.getMsg());
        }catch (ExistingPhoneNumException phoneNumException){
            return R.error(ExceptionCode.PHONE_EXIST_EXCEPTION.getCode(), ExceptionCode.PHONE_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    @PostMapping("/socialLogin")
    public R socialUserLogin(@RequestBody SocialUser vo) {
        MemberEntity memberEntity= memberService.login(vo);
        if (memberEntity == null) {
            return R.error(ExceptionCode.UNKNOWN_EXCEPTION.getCode(), ExceptionCode.UNKNOWN_EXCEPTION.getMsg());
        }else{
            return R.ok().put(AuthConstant.LOGIN_USER,memberEntity);
        }

    }
//a01c59ef-6e7a-4db1-94c1-7600f91e50fc
    @GetMapping("/session")
    public String getSession(HttpSession httpSession) {
        OauthMember oauthMember = new OauthMember();
        oauthMember.setNickname("test test");
        httpSession.setAttribute(AuthConstant.LOGIN_USER,oauthMember);
        return httpSession.getAttribute(AuthConstant.LOGIN_USER).toString();
    }
//7f3d7e15-5704-4b22-8fed-4b785b10323e
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo, HttpSession httpSession) {
        MemberEntity login = memberService.login(vo);
        if (login == null) {
            return R.error();
        }
        OauthMember oauthMember = new OauthMember();
        BeanUtils.copyProperties(login, oauthMember);
        httpSession.setAttribute(AuthConstant.LOGIN_USER,oauthMember);
        return R.ok().put("member",login);
    }

    @GetMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R r = couponFeignService.memberCoupons();

        return R.ok().put("member", memberEntity).put("coupons", r.get("coupons"));
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
