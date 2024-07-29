package com.xhpcd.rpc.client.cluster.loadBalance;

import com.xhpcd.rpc.annotation.RpcLoadBalance;
import com.xhpcd.rpc.client.cluster.LoadBalanceStrategy;
import com.xhpcd.rpc.client.cluster.StrategyProvider;
import com.xhpcd.rpc.client.config.RpcClientConfiguration;
import com.xhpcd.rpc.spring.SpringBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultStrategyProvider implements StrategyProvider {
    @Autowired
    private RpcClientConfiguration rpcClientConfiguration;
    @Override
    public LoadBalanceStrategy getStrategy() {

        return chose(rpcClientConfiguration.getRpcClientClusterStrategy());
    }

    public LoadBalanceStrategy chose(String strategy){
        Map<String, Object> beanByAnnotation = SpringBeanFactory.getBeanByAnnotation(RpcLoadBalance.class);
        for (Object value : beanByAnnotation.values()) {
            RpcLoadBalance annotation = value.getClass().getAnnotation(RpcLoadBalance.class);
            if(annotation.strategy().equals(strategy)){
                return (LoadBalanceStrategy)value;
            }
        }
        return SpringBeanFactory.getBean(RandomLoadBalanceStrategy.class);
    }
}
