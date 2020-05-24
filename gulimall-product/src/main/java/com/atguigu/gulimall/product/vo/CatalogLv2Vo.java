package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Justin
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CatalogLv2Vo {

    private String catalog1Id;

    private List<Cat3log3Vo> catalog3List;

    private String id;

    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Cat3log3Vo{

        private String catalog2Id;

        private String id;

        private String name;
    }

}
