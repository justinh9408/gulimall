package com.atguigu.gulimall.utils;

/**
 * @author: Justin
 */
public class RedisKeyUtils {

    public static final String UPLOAD_SEC_KILL_SKU_LOCK = "secKill:upload:lock";

    private static final String SEC_KILL_SESSION_CACHE_PREFIX = "secKill:session:";
    private static final String SEC_KILL_SKU_INFO_CACHE_PREFIX = "secKill:sku:";
    private static final String SEC_KILL_STOCK_SEMAPHORE = "secKill:stock:";

    public static String secKillSkuKey(Long sessionId, Long skuId) {
        return sessionId+"_"+skuId;
    }

    public static String userPurchaseKey(Long id, String killId) {
        return "purchaseKey:" + id + ":" + killId;
    }
}
