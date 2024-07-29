package com.xhpcd.rpc.util;

import com.xhpcd.rpc.common.ChannelMapping;
import com.xhpcd.rpc.netty.request.RequestPromise;

import java.util.HashMap;
import java.util.Map;

public class RpcHolder {
    private static final Map<String, RequestPromise> holder = new HashMap<>();

    private static final Map<String, ChannelMapping> mappingHolder = new HashMap<>();

    public static void set(String id,RequestPromise promise){
        holder.put(id,promise);
    }

    public static RequestPromise get(String id){
       return holder.remove(id);
    }

    public static boolean channelExist(String serverIp,int port){
        return getChannelMapping(serverIp,port)!=null;
    }

    public static ChannelMapping getChannelMapping(String serverIp,int port){
        return mappingHolder.get(serverIp+":"+port);
    }

    public static void setChannelMapping(ChannelMapping channelMapping){
        mappingHolder.put(channelMapping.getIpWithPort(),channelMapping);
    }
}
