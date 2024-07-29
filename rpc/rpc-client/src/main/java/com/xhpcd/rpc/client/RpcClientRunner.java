package com.xhpcd.rpc.client;


import com.xhpcd.rpc.client.discovery.RpcServerDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RpcClientRunner {
    @Autowired
    private RpcServerDiscovery rpcServerDiscovery;
    public void run(){

        rpcServerDiscovery.rpcServerDiscovery();
    }
}
