package com.xhpcd.rpc.server.registry.zk;

import com.xhpcd.rpc.server.boot.config.RpcServerConfiguration;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerZKit {
    @Autowired
    private ZkClient zkClient;

    @Autowired
    private RpcServerConfiguration rpcServerConfiguration;

    public void createRootNode(){
        boolean exists = zkClient.exists(rpcServerConfiguration.getZkRoot());
        if(!exists){
            zkClient.createPersistent(rpcServerConfiguration.getZkRoot());
        }
    }
    public void createPersistentNode(String path){
        boolean exists = zkClient.exists(rpcServerConfiguration.getZkRoot() + "/" + path);
        if(!exists){
            zkClient.createPersistent(rpcServerConfiguration.getZkRoot()+"/"+path);
        }
    }

    public void createNode(String path){
        path= rpcServerConfiguration.getZkRoot()+"/"+path;
        boolean exists = zkClient.exists(path);
        if(!exists){
            zkClient.createEphemeral(path);
        }
    }
}
