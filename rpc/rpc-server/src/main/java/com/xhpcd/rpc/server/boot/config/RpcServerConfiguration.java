package com.xhpcd.rpc.server.boot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class RpcServerConfiguration {
    @Value("${rpc.server.zk.root}")
    private String zkRoot;

    @Value("${rpc.server.zk.addr}")
    private String addr;

    @Value("${rpc.network.port}")
    private int rpcPort;

    @Value("${server.port}")
    private int serverPort;

    @Value("${rpc.server.zk.timeout}")
    private int timeOut;


}
