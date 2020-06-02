package com.atguigu.gulimall.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.feign.SkuFeignService;
import com.atguigu.gulimall.interceptor.UserStatusInterceptor;
import com.atguigu.gulimall.service.CartService;
import com.atguigu.gulimall.to.UserStatusTo;
import com.atguigu.gulimall.vo.Cart;
import com.atguigu.gulimall.vo.CartItem;
import com.atguigu.gulimall.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author: Justin
 */
@Service("CartService")
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    SkuFeignService skuFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String obj = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(obj)) {
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> skuInfoTask = CompletableFuture.runAsync(() -> {
//        远程查询sku信息
                R r = skuFeignService.skuInfo(skuId);
                SkuInfoVo skuInfo = r.getDataInType("skuInfo", SkuInfoVo.class);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setCount(num);
                cartItem.setImg(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setSkuId(skuId);
            },executor);

            CompletableFuture<Void> attrStringTask = CompletableFuture.runAsync(() -> {
//        远程调用查询attr string
                List<String> attrs = skuFeignService.skuAttrString(skuId);
                cartItem.setSkuAttr(attrs);
            },executor);
//阻塞
            CompletableFuture.allOf(skuInfoTask, attrStringTask).get();

            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);

            return cartItem;
        }else {
//            购物车已有改商品
            CartItem cartItem = JSON.parseObject(obj, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));

            return cartItem;
        }
    }

    @Override
    public CartItem getItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String obj = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(obj, CartItem.class);

        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserStatusTo status = UserStatusInterceptor.userStatus.get();
        if (status.getUserId() != null) {
            Cart cart = new Cart();
//            已登录
            String cartKey = CART_PREFIX + status.getUserId();
            String temp_cartKey = CART_PREFIX + status.getUserKey();
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(temp_cartKey);
            if (ops != null && ops.size() > 0) {
                List<CartItem> temp_carItems = getCartItems(temp_cartKey);
                for (CartItem temp : temp_carItems) {
                    addToCart(temp.getSkuId(), temp.getCount());
                }
                redisTemplate.delete(temp_cartKey);
            }
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

            return cart;
        }else{
            Cart cart = new Cart();
//            未登录
            String cartKey = CART_PREFIX + status.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

            return cart;
        }
    }

    @Override
    public List<CartItem> getCurrentUserCartItems() {
        UserStatusTo status = UserStatusInterceptor.userStatus.get();
        if (status.getUserId() == null) {
            return null;
        }

        String cartKey = CART_PREFIX + status.getUserId();
        List<CartItem> cartItems = getCartItems(cartKey);
        List<CartItem> items = cartItems.stream().filter(CartItem::getChecked)
                .map(item -> {
                    R r = skuFeignService.skuInfo(item.getSkuId());
                    SkuInfoVo skuInfo = r.getDataInType("skuInfo", SkuInfoVo.class);
                    item.setPrice(skuInfo.getPrice());
                    return item;
                }).collect(Collectors.toList());
        return items;
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        return values.stream().map(item -> {
//            String s = JSON.toJSONString(item);
            CartItem cartItem = JSON.parseObject((String)item, CartItem.class);
            return cartItem;
        }).collect(Collectors.toList());
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserStatusTo status = UserStatusInterceptor.userStatus.get();
        String cartkey = "";
        if (status.getUserId() != null) {
            cartkey = CART_PREFIX + status.getUserId();
        } else {
            cartkey = CART_PREFIX + status.getUserKey();
        }
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartkey);

        return ops;
    }
}
