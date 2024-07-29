package com.xhpcd.rpc.client.cache;

import com.google.common.cache.LoadingCache;
import com.xhpcd.rpc.client.provider.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class DefaultServiceProviderCache implements ServiceProviderCache{
    @Autowired
    private LoadingCache<String,List<ServiceProvider>> loadingCache;
    @Override
    public void put(String key, List<ServiceProvider> value) {
      loadingCache.put(key,value);
    }

    @Override
    public List<ServiceProvider> get(String key) {
        try {
            return loadingCache.get(key);
        } catch (ExecutionException e) {
            return new ArrayList<ServiceProvider>();
        }
    }

    @Override
    public void evict(String key) {
       loadingCache.invalidate(key);
    }

    @Override
    public void update(String key, List<ServiceProvider> list) {
        evict(key);
        put(key,list);
    }
}
