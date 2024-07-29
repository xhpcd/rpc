package com.xhpcd.rpc.server.boot.config;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerZkCliConfig {
    @Autowired
    private RpcServerConfiguration rpcServerConfiguration;
    @Bean
    public ZkClient zkClient(){

        return new ZkClient(rpcServerConfiguration.getAddr(),rpcServerConfiguration.getTimeOut());
    }
}
