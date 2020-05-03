package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.ExceptionCode;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.UnexpectedTypeException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice("com.atguigu.gulimall.product.controller")
public class ExceptionControlerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R validationExp(MethodArgumentNotValidException e) {
        log.error("数据校验错误: {}", e.getLocalizedMessage());

        BindingResult result = e.getBindingResult();
        Map<String, String> map = new HashMap<>();
        result.getFieldErrors().forEach((item) -> {
            map.put(item.getField(), item.getDefaultMessage());
        });

        return R.error(ExceptionCode.VALIDATION_EXCEPTION.getCode(), ExceptionCode.VALIDATION_EXCEPTION.getMsg()).put("data", map);
    }


//    @ExceptionHandler(Throwable.class)
//    public R exception(Throwable throwable) {
//        log.error("未知错误: {}{}", throwable.getLocalizedMessage(),throwable.getCause());
//
//        return R.error(ExceptionCode.UNKNOWN_EXCEPTION.getCode(), ExceptionCode.UNKNOWN_EXCEPTION.getMsg());
//    }
}
