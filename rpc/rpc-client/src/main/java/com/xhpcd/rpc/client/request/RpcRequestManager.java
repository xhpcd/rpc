package com.xhpcd.rpc.client.request;

import com.xhpcd.rpc.client.cache.ServiceProviderCache;
import com.xhpcd.rpc.common.ChannelMapping;
import com.xhpcd.rpc.data.RpcRequest;
import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.handler.RpcResponseHandler;
import com.xhpcd.rpc.netty.codec.FrameDecoder;
import com.xhpcd.rpc.netty.codec.FrameEncoder;
import com.xhpcd.rpc.netty.codec.RpcRequestEncoder;
import com.xhpcd.rpc.netty.codec.RpcResponseDecoder;
import com.xhpcd.rpc.netty.request.RequestPromise;
import com.xhpcd.rpc.provider.ServiceProvider;
import com.xhpcd.rpc.util.RpcHolder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

@Component
public class RpcRequestManager {
    @Autowired
    private ServiceProviderCache serviceProviderCache;

    public RpcResponse sendRequest(RpcRequest request) {
        List<ServiceProvider> serviceProviders = serviceProviderCache.get(request.getClassName());
        ServiceProvider serviceProvider = serviceProviders.get(0);

        return doSendRequest(request,serviceProvider);
    }

    public RpcResponse doSendRequest(RpcRequest request,ServiceProvider serviceProvider){

        Channel channel;
        if(!RpcHolder.channelExist(serviceProvider.getServerIp(),serviceProvider.getRcpPort())){
            NioEventLoopGroup group = new NioEventLoopGroup(NettyRuntime.availableProcessors()*2);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("FrameDecoder",new FrameDecoder());
                            pipeline.addLast("RpcResponseDecoder",new RpcResponseDecoder());
                            pipeline.addLast("FrameEncoder",new FrameEncoder());
                            pipeline.addLast("RpcRequestEncoder",new RpcRequestEncoder());
                            pipeline.addLast("RpcResponseHandler",new RpcResponseHandler());
                        }
                    });
            try {
                ChannelFuture future = bootstrap.connect(new InetSocketAddress(serviceProvider.getServerIp(), serviceProvider.getRcpPort())).sync();
                if (future.isSuccess()) {
                    channel = future.channel();
                    RpcHolder.setChannelMapping(new ChannelMapping(serviceProvider.getServerIp(), serviceProvider.getRcpPort(), channel));
                }
            }catch (Exception e){
                group.shutdownGracefully();
            }
        }


        try {

            ChannelMapping channelMapping = RpcHolder.getChannelMapping(serviceProvider.getServerIp(), serviceProvider.getRcpPort());
            channel = channelMapping.getChannel();
            channel.writeAndFlush(request);
                RequestPromise requestPromise = new RequestPromise(channel.eventLoop());
                RpcHolder.set(request.getRequestId(),requestPromise);
                RpcResponse rpcResponse = (RpcResponse) requestPromise.get();
                return rpcResponse;

        }catch (Exception e){
            e.printStackTrace();
        }
        return new RpcResponse();
    }
}
