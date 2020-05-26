package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author: Justin
 */
@Data
public class MemberRegisterVo {

    private String userName;


    private String password;


    private String phoneNum;

}
