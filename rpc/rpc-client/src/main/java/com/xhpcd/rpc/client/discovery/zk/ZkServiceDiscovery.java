package com.xhpcd.rpc.client.discovery.zk;

import com.xhpcd.rpc.client.cache.ServiceProviderCache;
import com.xhpcd.rpc.client.discovery.RpcServerDiscovery;
import com.xhpcd.rpc.client.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ZkServiceDiscovery implements RpcServerDiscovery {
    @Autowired
    private ClientZKit clientZKit;
    @Autowired
    private ServiceProviderCache serviceProviderCache;
    @Override
    public void rpcServerDiscovery() {

        List<String> service = clientZKit.getService();
        for (String s : service) {
            List<ServiceProvider> serviceInfos = clientZKit.getServiceInfos(s);
            serviceProviderCache.put(s,serviceInfos);
            clientZKit.subscribeZKNode(s);
            log.info("client subscribe {},services{}",s,serviceInfos);
        }
    }
}
