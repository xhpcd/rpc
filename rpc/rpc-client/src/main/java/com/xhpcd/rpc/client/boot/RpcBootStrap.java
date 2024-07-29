package com.xhpcd.rpc.client.boot;


import com.xhpcd.rpc.client.RpcClientRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RpcBootStrap {
    @Autowired
    private RpcClientRunner rpcClientRunner;
    @PostConstruct
    public void initRpcClient(){
        rpcClientRunner.run();
    }
}
