package com.xhpcd.rpc.client.cluster.loadBalance;


import com.xhpcd.rpc.annotation.RpcLoadBalance;
import com.xhpcd.rpc.client.cluster.LoadBalanceStrategy;
import com.xhpcd.rpc.client.provider.ServiceProvider;

import java.util.List;
import java.util.Random;

@RpcLoadBalance(strategy = "random")
public class RandomLoadBalanceStrategy implements LoadBalanceStrategy {

    static Random random = new Random();
    @Override
    public ServiceProvider select(List<ServiceProvider> list) {
        return list.get(random.nextInt(list.size()));
    }
}
