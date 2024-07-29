package com.xhpcd.rpc.client.cluster;

public interface StrategyProvider {
    LoadBalanceStrategy getStrategy();
}
