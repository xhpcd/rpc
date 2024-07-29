package com.xhpcd.rpc.client.cache;

import com.xhpcd.rpc.client.provider.ServiceProvider;

import java.util.List;

public interface ServiceProviderCache {
    public void put(String key, List<ServiceProvider> value);
    List<ServiceProvider> get(String key);
    void evict(String key);
    void update(String key,List<ServiceProvider> list);
}
