package com.atguigu.gulimall.controller;

import com.atguigu.gulimall.service.CartService;
import com.atguigu.gulimall.vo.Cart;
import com.atguigu.gulimall.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author: Justin
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @ResponseBody
    @GetMapping("/cartItems")
    public List<CartItem> cartItems() {
        List<CartItem>  items = cartService.getCurrentUserCartItems();

        return items;
    }

    @GetMapping("/cart.html")
    public String cartPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cartList", cart);

        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        CartItem cartItem = cartService.addToCart(skuId, num);
        ra.addAttribute("skuId",skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess";
    }

    @GetMapping("/addToCartSuccess")
    public String success(@RequestParam("skuId")Long skuId,Model model) {
        CartItem cartItem = cartService.getItem(skuId);
        model.addAttribute("item", cartItem);

        return "success";
    }
}
