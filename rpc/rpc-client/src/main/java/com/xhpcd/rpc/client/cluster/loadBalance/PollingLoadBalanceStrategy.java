package com.xhpcd.rpc.client.cluster.loadBalance;

import com.xhpcd.rpc.annotation.RpcLoadBalance;
import com.xhpcd.rpc.client.cluster.LoadBalanceStrategy;
import com.xhpcd.rpc.client.provider.ServiceProvider;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RpcLoadBalance(strategy = "polling")
public class PollingLoadBalanceStrategy implements LoadBalanceStrategy {
    private AtomicInteger index = new AtomicInteger(0);
    @Override
    public ServiceProvider select(List<ServiceProvider> list) {
        int i = computeNextIndex(list.size());
        return list.get(i);
    }

    public int computeNextIndex(int size){
        for(;;){
            int i = index.get();
            int next = (i+1)%size;
            if(index.compareAndSet(i,next)){
                return i;
            }
        }
    }
}
