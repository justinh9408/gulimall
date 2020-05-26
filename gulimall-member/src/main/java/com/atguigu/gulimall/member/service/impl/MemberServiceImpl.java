package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.exception.ExistingPhoneNumException;
import com.atguigu.gulimall.member.exception.ExistingUserNameException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    public boolean login(MemberLoginVo vo) {
        String acc = vo.getAcc();
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("mobile", acc)
                .or().eq("username", acc));
        if (memberEntity != null) {
            String password = vo.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(password, memberEntity.getPassword());
            if (matches) {
                return true;
            }
        }

        return false;
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