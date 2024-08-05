package com.xhpcd.rpc.client.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.xhpcd.rpc.client.provider.ServiceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CacheConfig {
    @Bean
    public LoadingCache<String, List<ServiceProvider>> loadingCache(){
        return CacheBuilder.newBuilder()
              //  .refreshAfterWrite(1, TimeUnit.MINUTES) // 设置1分钟后刷新
                .build(new CacheLoader<String, List<ServiceProvider>>() {
                    @Override
                    public List<ServiceProvider> load(String key) {
                        return new ArrayList<ServiceProvider>(); // 模拟从数据库加载数据
                    }
                });
    }
}
