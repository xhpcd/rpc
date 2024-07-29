package com.xhpcd.rpc.server.boot;

import com.xhpcd.rpc.server.boot.config.RpcServerConfiguration;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RpcServerBootstrap {
    @Autowired
    private RpcServerRunner rpcServerRunner;



    @PostConstruct
    public void init(){
       rpcServerRunner.run();
    }
}
