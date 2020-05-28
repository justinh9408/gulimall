package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.CatalogLv2Vo;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @author: Justin
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model, HttpSession httpSession) {
        List<CategoryEntity> lv1Categories = categoryService.findLevel1Categories();
        model.addAttribute("categories", lv1Categories);

        return "index";
    }

    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<CatalogLv2Vo>> getCataLog() {
        Map<String, List<CatalogLv2Vo>> json = categoryService.getCatalogJson();

        return json;
    }

    @GetMapping("/hello")
    public String Hello() {
        RLock lock = redissonClient.getLock("lock-1");
        lock.lock();
        try {
            System.out.println("业务代码");
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return "hello";
    }

//Sempaphor可以用作于分布式限流
    @GetMapping("/park")
    @ResponseBody
    public String park() {
        RSemaphore park = redissonClient.getSemaphore("park");
        boolean b = park.tryAcquire();//获取一个信号量，占一个车位
        if (b) {
            //业务...
        } else {
            //error
        }

        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();//释放一个信号量，空一个车位

        return "release";
    }

    @GetMapping("/lock")
    @ResponseBody
    public String lock() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();

        return "door locked";
    }

    @GetMapping("/leave/{id}")
    @ResponseBody
    public String leave(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();

        return id + "leave";
    }
}
