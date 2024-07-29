package com.xhpcd.shop.controller.order;


import com.xhpcd.rpc.annotation.RpcRemote;
import com.xhpcd.shop.order.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {


    @RpcRemote
    private OrderService orderService;


    @GetMapping("/getOrder")
    public String getOrder(String userId,String orderId){
        return orderService.getOrder(userId,orderId);
    }

}
