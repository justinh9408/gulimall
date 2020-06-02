package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.NotEnoughStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import com.atguigu.gulimall.order.vo.SubmitOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author: Justin
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("confirm", confirmVo);

        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(SubmitOrderVo vo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        try {
            responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode() != 0) {
                String msg = "";
                switch (responseVo.getCode().toString()) {
                    case "1":
                        msg = "令牌错误";
                        break;
                    case "2":
                        msg = "验价错误";
                        break;
                    case "3":
                        msg = "锁库存失败";
                        break;
                }
                redirectAttributes.addFlashAttribute("error", msg);
                return "redirect:http://order.gulimall.com/toTrade";
            } else {
                model.addAttribute("submitResp", responseVo);
                return "pay";
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof NotEnoughStockException) {
                redirectAttributes.addFlashAttribute("error", "锁库存失败");
            }
            redirectAttributes.addFlashAttribute("error", "未知错误");
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
