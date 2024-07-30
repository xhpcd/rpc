package com.xhpcd.rpc.client.discovery.zk;


import com.xhpcd.rpc.client.cache.ServiceProviderCache;
import com.xhpcd.rpc.client.config.RpcClientConfiguration;
import com.xhpcd.rpc.client.provider.ServiceProvider;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClientZKit {
    @Autowired
    private RpcClientConfiguration configuration;

    @Autowired
    private ServiceProviderCache serviceProviderCache;

    @Autowired
    private ZkClient zkClient;

    //获取所有的服务信息

    public List<String> getService(){
        String zkRoot = configuration.getZkRoot();
        List<String> children = zkClient.getChildren(zkRoot);
        return children;
    }

    public void subscribeZKNode(String name){
        String node = configuration.getZkRoot() + "/" + name;
        zkClient.subscribeChildChanges(node, new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                if(!CollectionUtils.isEmpty(list)){
                    List<ServiceProvider> serviceProviders = convertToProviderService(s, list);
                    serviceProviderCache.update(s,serviceProviders);
                }
            }
        });
    }
    public List<ServiceProvider> getServiceInfos(String serviceName){
        String path = configuration.getZkRoot() + "/" + serviceName;
        List<String> children = zkClient.getChildren(path);
        List<ServiceProvider> serviceProviders = convertToProviderService(serviceName, children);
        return serviceProviders;
    }

    public List<ServiceProvider> convertToProviderService(String serviceName,List<String> list){
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<ServiceProvider>();
        }
        List<ServiceProvider> list1 = list.stream().map(v -> {
            String[] split = v.split(":");
            return ServiceProvider.builder().serviceName(serviceName).serverIp(split[0])
                    .rcpPort(Integer.parseInt(split[1])).weight(Integer.parseInt(split[2])).build();
        }).collect(Collectors.toList());
        return list1;
    }
}
