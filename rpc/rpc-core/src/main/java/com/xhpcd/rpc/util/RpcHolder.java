package com.xhpcd.rpc.util;

import com.xhpcd.rpc.common.ChannelMapping;
import com.xhpcd.rpc.netty.request.RequestPromise;

import java.util.HashMap;
import java.util.Map;

public class RpcHolder {
    /**
     * 由于Netty的异步线程要和Netty外部线程做数据同步 所以需要Promise 避免混乱通过holder+requestID进行判断
     */
    private static final Map<Long, RequestPromise> holder = new HashMap<>();

    private static final Map<String, ChannelMapping> mappingHolder = new HashMap<>();

    public static void set(Long id,RequestPromise promise){
        holder.put(id,promise);
    }

    public static RequestPromise get(Long id){
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
    public static void removeChannelMapping(ChannelMapping channelMapping){
        mappingHolder.remove(channelMapping.getIpWithPort());
    }
}
