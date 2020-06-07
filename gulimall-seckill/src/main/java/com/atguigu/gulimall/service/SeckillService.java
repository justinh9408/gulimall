package com.atguigu.gulimall.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.OauthMember;
import com.atguigu.gulimall.feign.CouponFeignService;
import com.atguigu.gulimall.feign.ProductFeignService;
import com.atguigu.gulimall.interceptor.LoginWebInterceptor;
import com.atguigu.gulimall.to.SecKillSkuRedisTo;
import com.atguigu.gulimall.to.SkuInfo;
import com.atguigu.gulimall.utils.RedisKeyUtils;
import com.atguigu.gulimall.vo.SecKillSessionWithSkusVo;
import com.atguigu.gulimall.vo.SecKillSkuVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: Justin
 */
@Service
public class SeckillService {

    private static final String SEC_KILL_SESSION_CACHE_PREFIX = "secKill:session:";
    private static final String SEC_KILL_SKU_INFO_CACHE_PREFIX = "secKill:sku:";
    private static final String SEC_KILL_STOCK_SEMAPHORE = "secKill:stock:";

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public Boolean uploadLatest3DaysGoods() {
        R r = couponFeignService.latest3daysSessions();
        if (r.getCode() == 0) {
            Object session = r.get("sessions");
            String s = JSON.toJSONString(session);
            List<SecKillSessionWithSkusVo> sessionWithSkus = JSON.parseObject(s, new TypeReference<List<SecKillSessionWithSkusVo>>() {
            });

            saveSessionsInRedis(sessionWithSkus);

            saveSecKillSkusInRedis(sessionWithSkus);

            return true;
        } else {
            return false;
        }
    }

    public void saveSessionsInRedis(List<SecKillSessionWithSkusVo> sessions) {
        sessions.forEach(session -> {
            long start = session.getStartTime().getTime();
            long end = session.getEndTime().getTime();
            String key = SEC_KILL_SESSION_CACHE_PREFIX + start + ":" + end;
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelations()
                        .stream()
                        .map(relation -> {
                            String value = RedisKeyUtils.secKillSkuKey(session.getId(), relation.getSkuId());
                            return value;
                        })
                        .collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    public void saveSecKillSkusInRedis(List<SecKillSessionWithSkusVo> sessions) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SEC_KILL_SKU_INFO_CACHE_PREFIX);
        sessions.forEach(session -> {
            session.getRelations().stream().forEach(relation -> {
                SecKillSkuRedisTo skuRedisTo = new SecKillSkuRedisTo();
                String k = RedisKeyUtils.secKillSkuKey(session.getId(), relation.getSkuId());
                if (!hashOps.hasKey(k)) {
//                Sku基本信息
                    Long skuId = relation.getSkuId();
                    R r = productFeignService.getSkuInfo(skuId);
                    SkuInfo skuInfo = r.getDataInType("skuInfo", SkuInfo.class);
                    skuRedisTo.setSkuInfo(skuInfo);

//                秒杀基本信息
                    BeanUtils.copyProperties(relation, skuRedisTo);
                    skuRedisTo.setStartTime(session.getStartTime().getTime());
                    skuRedisTo.setEndTime(session.getEndTime().getTime());

//                随机码
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    skuRedisTo.setToken(randomCode);

//               设置分布式信号量，限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SEC_KILL_STOCK_SEMAPHORE + skuRedisTo.getToken());
                    semaphore.trySetPermits(relation.getSeckillCount().intValue());

//               保存到redis
                    String jsonString = JSON.toJSONString(skuRedisTo);
                    hashOps.put(k, jsonString);
                }
            });
        });
    }

    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        long now = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SEC_KILL_SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SEC_KILL_SESSION_CACHE_PREFIX, "");
            String[] split = replace.split(":");
            long start = Long.parseLong(split[0]);
            long end = Long.parseLong(split[1]);
            if (now >= start && now < end) {
//                获取当前场次的秒杀商品
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SEC_KILL_SKU_INFO_CACHE_PREFIX);
                List<String> skuJsonStrings = hashOps.multiGet(range);
                if (skuJsonStrings != null) {
                    List<SecKillSkuRedisTo> collect = skuJsonStrings.stream().map(s -> {
                        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(s, SecKillSkuRedisTo.class);
                        return secKillSkuRedisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
            }
        }
        return null;
    }

    public SecKillSkuRedisTo seckillSkuInfo(Long skuId) {
        String regx = "\\d_" + skuId;
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SEC_KILL_SKU_INFO_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        for (String key : keys) {
            if (Pattern.matches(regx, key)) {
                String json = hashOps.get(key);
                SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
                long now = new Date().getTime();
                Long startTime = secKillSkuRedisTo.getStartTime();
                if (startTime > now) {
                    secKillSkuRedisTo.setToken(null);
                }
                return secKillSkuRedisTo;
            }
        }
        return null;
    }

    /**
     * 秒杀商品
     *
     * @param killId sessionId_skuId
     * @param token random code
     * @param num   kill counts
     * @return
     */
    public String kill(String killId, String token, Long num) {
        OauthMember member = LoginWebInterceptor.loginUser.get();
        BoundHashOperations<String, String, String> hashops = redisTemplate.boundHashOps(SEC_KILL_SKU_INFO_CACHE_PREFIX);
        String json = hashops.get(killId);
        if (!StringUtils.isEmpty(json)) {
            SecKillSkuRedisTo sku = JSON.parseObject(json, SecKillSkuRedisTo.class);
            long now = new Date().getTime();
            Long startTime = sku.getStartTime();
            Long endTime = sku.getEndTime();
            Long ttl = endTime - now;
//            合法性判断
            if (now >= startTime && now < endTime && token.equals(sku.getToken())) {
//                判断是否抢购过
                String purchaseKey = RedisKeyUtils.userPurchaseKey(member.getId(), killId);
                Boolean absent = redisTemplate.opsForValue().setIfAbsent(purchaseKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                if (absent) {
//                    抢占semaphore
                    String semKey = SEC_KILL_STOCK_SEMAPHORE + token;
                    RSemaphore semaphore = redissonClient.getSemaphore(semKey);
                    boolean acquired = semaphore.tryAcquire(num.intValue());
                    if (acquired) {
//                        秒杀成功
//                        生成订单号
                        String orderSn = IdWorker.getTimeId();
//                        快速下单，加入MQ, 流量削峰
                        SecKillOrderTo secKillOrderTo = new SecKillOrderTo();
                        secKillOrderTo.setMemberId(member.getId());
                        secKillOrderTo.setNum(num);
                        secKillOrderTo.setOrderSn(orderSn);
                        secKillOrderTo.setPromotionSessionId(sku.getPromotionId());
                        secKillOrderTo.setSeckillPrice(sku.getSeckillPrice());
                        rabbitTemplate.convertAndSend("order-event-exchange", "order.secKill", secKillOrderTo);

                        return orderSn;
                    }
                }
            }
        }
        return null;
    }
}
