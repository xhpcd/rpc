package com.xhpcd.rpc.client.cluster.loadBalance;

import com.xhpcd.rpc.client.cluster.LoadBalanceStrategy;
import com.xhpcd.rpc.client.provider.ServiceProvider;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class WeightRoundStrategy implements LoadBalanceStrategy {
    @Override
    public ServiceProvider select(List<ServiceProvider> list) {
        return null;
    }
}
