package com.xhpcd.rpc.client.config;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class zkClientConfig {
    @Autowired
    private RpcClientConfiguration rpcClientConfiguration;

    @Bean
    public ZkClient zkClient(){
        return new ZkClient(rpcClientConfiguration.getAddr(),rpcClientConfiguration.getTimeOut());
    }
}
