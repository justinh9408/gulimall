package com.atguigu.gulimall.schedule;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: Justin
 */
@Component
public class HelloScheduler {

//    @Scheduled(cron = "* * * * * MON-FRI")
    public void hello() {
        System.out.println("hello");
    }
}
