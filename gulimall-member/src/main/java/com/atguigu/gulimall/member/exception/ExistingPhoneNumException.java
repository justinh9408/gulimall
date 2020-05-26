package com.atguigu.gulimall.member.exception;

/**
 * @author: Justin
 */
public class ExistingPhoneNumException extends RuntimeException{

    public ExistingPhoneNumException() {
        super("电话号码已存在");
    }
}
