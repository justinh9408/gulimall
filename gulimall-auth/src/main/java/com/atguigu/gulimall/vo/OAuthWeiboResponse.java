package com.atguigu.gulimall.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author: Justin
 */
@ToString
@Data
public class OAuthWeiboResponse {

    private String access_token;
    private String remind_in;
    private String expires_in;
    private Long uid;
}
