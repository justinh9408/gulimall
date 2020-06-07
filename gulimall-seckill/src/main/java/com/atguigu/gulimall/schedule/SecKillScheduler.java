package com.atguigu.gulimall.schedule;

import com.atguigu.gulimall.service.SeckillService;
import com.atguigu.gulimall.utils.RedisKeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author: Justin
 */
@Slf4j
@Service
public class SecKillScheduler {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    @Scheduled(cron = "5/60 * * * * ?")
    public void uploadLatest3DaysGoods(){
        RLock lock = redissonClient.getLock(RedisKeyUtils.UPLOAD_SEC_KILL_SKU_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            log.info("上架秒杀商品......");
            seckillService.uploadLatest3DaysGoods();
        } finally {
            lock.unlock();
        }
    }
}
