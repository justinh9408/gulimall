package com.atguigu.common.exception;

public enum  ExceptionCode {

    VALIDATION_EXCEPTION(10001,"校验错误"),
    UNKNOWN_EXCEPTION(10000,"未知错误");

    private int code;
    private String msg;

//    私有构造器
    ExceptionCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
