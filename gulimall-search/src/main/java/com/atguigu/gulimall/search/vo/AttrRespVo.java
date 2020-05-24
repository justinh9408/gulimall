package com.atguigu.gulimall.search.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

    private Long attrGroupId;
}
