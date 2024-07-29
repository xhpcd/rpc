package com.xhpcd.rpc.client.cluster.loadBalance;

import com.xhpcd.rpc.annotation.RpcLoadBalance;
import com.xhpcd.rpc.client.cluster.LoadBalanceStrategy;
import com.xhpcd.rpc.common.IpUtils;
import com.xhpcd.rpc.client.provider.ServiceProvider;

import java.util.List;

@RpcLoadBalance(strategy = "ipHash")
public class HashLoadBalanceStrategy implements LoadBalanceStrategy {
    String local = IpUtils.getRealIp();
    @Override
    public ServiceProvider select(List<ServiceProvider> list) {
       return list.get(local.hashCode()%list.size());
    }
}
