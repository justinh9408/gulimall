package com.atguigu.gulimall;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GulimallOrderApplication.class})
public class GulimallOrderApplicationTest {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Test
    public void testRabbit() {
        DirectExchange directExchange = new DirectExchange("test-direct-exchange");
        amqpAdmin.declareExchange(directExchange);
    }
}