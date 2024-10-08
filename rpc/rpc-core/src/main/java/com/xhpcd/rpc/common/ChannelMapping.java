package com.xhpcd.rpc.common;


import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 对客户端的channel进行缓存 避免每次调用都进行三次握手创建连接
 */
@Data
@Slf4j
public class ChannelMapping {

    private String ip;

    private int port;

    private Channel channel;

    public ChannelMapping(String ip,int port,Channel channel){
        this.channel = channel;
        this.ip = ip;
        this.port = port;
    }

    public String getIpWithPort(){
        return ip+":"+port;
    }

    @Override
    public boolean equals(Object o){
        if(this == o)return true;
        if(o==null || getClass() != o.getClass())return false;
        ChannelMapping channelMapping = (ChannelMapping) o;
        return this.port == channelMapping.getPort()&&this.ip.equals(channelMapping.getIp());

    }
}
