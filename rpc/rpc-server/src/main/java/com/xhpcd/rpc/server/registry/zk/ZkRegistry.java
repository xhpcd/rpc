package com.xhpcd.rpc.server.registry.zk;

import com.xhpcd.rpc.annotation.RpcService;
import com.xhpcd.rpc.common.IpUtils;
import com.xhpcd.rpc.server.boot.RpcServer;
import com.xhpcd.rpc.server.boot.config.RpcServerConfiguration;
import com.xhpcd.rpc.server.registry.RpcRegistry;
import com.xhpcd.rpc.spring.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@DependsOn("springBeanFactory")
public class ZkRegistry implements RpcRegistry {

    @Autowired
    private ServerZKit serverZKit;

    @Autowired
    private RpcServerConfiguration rpcServerConfiguration;

    @Autowired
    private RpcServer rpcServer;
    @Override
    public void serviceRegistry() {
        Map<String, Object> beanByAnnotation = SpringBeanFactory.getBeanByAnnotation(RpcService.class);
        if(beanByAnnotation!=null&&!beanByAnnotation.isEmpty()) {
            serverZKit.createRootNode();
            String serverIp = IpUtils.getRealIp();
            for (Map.Entry<String, Object> entry : beanByAnnotation.entrySet()) {
                RpcService annotation = entry.getValue().getClass().getAnnotation(RpcService.class);
                Class<?> interfaceClass = annotation.interfaceClass();
                //服务名称
                String name = interfaceClass.getName();
                serverZKit.createPersistentNode(name);
                String providerNode = serverIp+":"+rpcServerConfiguration.getRpcPort();
                serverZKit.createNode(name+"/"+providerNode);
                log.info("服务{}-{}完成了注册",name,providerNode);
            }
            rpcServer.start();
        }
    }
}
