package com.xhpcd.rpc.client.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class RpcClientConfiguration {
    @Value("${rpc.client.zk.root}")
    private String zkRoot;


    @Value("${rpc.client.zk.addr}")
    private String addr;


    @Value("${rpc.cluster.strategy}")
    private String rpcClientClusterStrategy;

    @Value("${server.port}")
    private int serverPort;

    @Value("${rpc.client.zk.timeout}")
    private int timeOut;



}
