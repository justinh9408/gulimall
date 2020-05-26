package com.atguigu.gulimall.member.exception;

/**
 * @author: Justin
 */
public class ExistingUserNameException extends RuntimeException{

    public ExistingUserNameException() {
        super("用户名已存在");

    }
}
