package com.atguigu.gulimall.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author: Justin
 */
@Data
public class RegisterVo {

    @NotEmpty(message = "userName必须填写")
    private String userName;

    @NotEmpty(message = "password必须填写")
    @Length(min = 8,max = 16,message = "长度不对")
    private String password;

    @NotEmpty(message = "phoneNum必须填写")
    @Pattern(regexp = "^[1][0-9]{10}",message = "手机格式错误")
    private String phoneNum;

    @NotEmpty(message = "code必须填写")
    private String code;
}
