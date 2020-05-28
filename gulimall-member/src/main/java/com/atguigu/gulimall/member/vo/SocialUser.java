package com.atguigu.gulimall.member.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author: Justin
 */
@ToString
@Data
public class SocialUser {

    private String access_token;
    private String remind_in;
    private String expires_in;
    private Long uid;
}
