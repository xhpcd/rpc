package com.xhpcd.shop.order.service;

import com.xhpcd.rpc.annotation.RpcService;
import com.xhpcd.shop.order.OrderService;

@RpcService(interfaceClass = OrderService.class,weight = 2)
public class OrderServiceImpl implements OrderService {

    @Override
    public String getOrder(String userId, String orderId) {
        return "user: "+userId+"orderId:"+orderId;
    }
}
