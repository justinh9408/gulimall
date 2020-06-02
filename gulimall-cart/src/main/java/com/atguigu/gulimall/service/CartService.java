package com.atguigu.gulimall.service;

import com.atguigu.gulimall.vo.Cart;
import com.atguigu.gulimall.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    List<CartItem> getCurrentUserCartItems();

}
