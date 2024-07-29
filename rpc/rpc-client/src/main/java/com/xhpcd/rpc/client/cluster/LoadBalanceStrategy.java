package com.xhpcd.rpc.client.cluster;

import com.xhpcd.rpc.client.provider.ServiceProvider;

import java.util.List;

public interface LoadBalanceStrategy {
    ServiceProvider select(List<ServiceProvider> list);
}
