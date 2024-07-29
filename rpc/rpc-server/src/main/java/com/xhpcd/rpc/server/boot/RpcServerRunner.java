package com.xhpcd.rpc.server.boot;


import com.xhpcd.rpc.server.registry.RpcRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RpcServerRunner {
    @Autowired
    private RpcRegistry rpcRegistry;

    public void run(){
        rpcRegistry.serviceRegistry();
    }
}
