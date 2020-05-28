package com.atguigu.gulimall.to;

import lombok.Data;
import lombok.ToString;

/**
 * @author: Justin
 */
@Data
@ToString
public class UserStatusTo {

    private Long userId;
    private String userKey;
    private Boolean temp_user = false;
}
