package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.member.exception.ExistingPhoneNumException;
import com.atguigu.gulimall.member.exception.ExistingUserNameException;
import com.atguigu.gulimall.member.utils.HttpUtil;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) throws ExistingUserNameException,ExistingPhoneNumException{
        MemberEntity memberEntity = new MemberEntity();

        checkUserNameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhoneNum());

        memberEntity.setLevelId(1L);

        memberEntity.setCreateTime(new Date());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setMobile(vo.getPhoneNum());

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

//        保存
        this.baseMapper.insert(memberEntity);
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String acc = vo.getAcc();
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("mobile", acc)
                .or().eq("username", acc));
        if (memberEntity != null) {
            String password = vo.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(password, memberEntity.getPassword());
            if (matches) {
                memberEntity.setNickname(memberEntity.getUsername());
                return memberEntity;
            }
        }

        return null;
    }

    @Override
    public MemberEntity login(SocialUser vo) {
        Long uid = vo.getUid();
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("uid", uid));
        if (memberEntity != null) {
//            已存在账户
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setExpires_in(vo.getExpires_in());
            update.setAccess_token(vo.getAccess_token());
            this.baseMapper.updateById(update);

            memberEntity.setExpires_in(vo.getExpires_in());
            memberEntity.setAccess_token(vo.getAccess_token());

            return memberEntity;

        } else {
//            新建账户
            MemberEntity newMember = new MemberEntity();
            newMember.setAccess_token(vo.getAccess_token());
            newMember.setExpires_in(vo.getExpires_in());
            newMember.setUid(vo.getUid());

//https://api.weibo.com/2/users/show.json?uid=1913287700&access_token=2.00CXxTFCVBijpDf1d794d47b0YN137
            HashMap<String, String > map = new HashMap<>();
            map.put("access_token", vo.getAccess_token());
            map.put("uid", vo.getUid().toString());
            String result = "";
            try {
                String url = HttpUtil.appendQueryParams("https://api.weibo.com/2/users/show.json", map);
                result = HttpUtil.get(url);
            } catch (Exception e) {
                return null;
            }

            JSONObject jsonObject = JSON.parseObject(result);
            String  name = (String) jsonObject.get("name");
            newMember.setNickname(name);

            this.baseMapper.insert(newMember);
            return newMember;
        }

    }

    private void checkUserNameUnique(String userName) throws ExistingUserNameException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new ExistingUserNameException();
        }
    }

    private void checkPhoneUnique(String phoneNum) throws ExistingPhoneNumException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phoneNum));
        if (count > 0) {
            throw new ExistingPhoneNumException();
        }
    }

}