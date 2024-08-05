package com.xhpcd.rpc.client.request;

import com.xhpcd.rpc.client.cache.ServiceProviderCache;
import com.xhpcd.rpc.client.cluster.LoadBalanceStrategy;
import com.xhpcd.rpc.client.cluster.StrategyProvider;
import com.xhpcd.rpc.client.config.RpcClientConfiguration;
import com.xhpcd.rpc.common.ChannelMapping;
import com.xhpcd.rpc.data.PingMessage;
import com.xhpcd.rpc.data.RpcRequest;
import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.handler.RpcResponseHandler;
import com.xhpcd.rpc.netty.codec.*;
import com.xhpcd.rpc.netty.request.RequestPromise;
import com.xhpcd.rpc.client.provider.ServiceProvider;
import com.xhpcd.rpc.util.RpcHolder;
import com.xhpcd.rpc.util.UniqueIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

@Component
@Slf4j
public class RpcRequestManager {
    @Autowired
    private ServiceProviderCache serviceProviderCache;
    @Autowired
    private StrategyProvider strategyProvider;
    @Autowired
    private RpcClientConfiguration rpcClientConfiguration;

    public RpcResponse sendRequest(RpcRequest request) {
        List<ServiceProvider> serviceProviders = serviceProviderCache.get(request.getClassName());
        LoadBalanceStrategy strategy = strategyProvider.getStrategy();
        ServiceProvider serviceProvider = strategy.select(serviceProviders);
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
                            pipeline.addLast("FrameEncoder",new FrameEncoder());
                            /*pipeline.addLast("RpcResponseDecoder",new RpcResponseDecoder());

                            pipeline.addLast("RpcRequestEncoder",new RpcRequestEncoder());*/
                            pipeline.addLast("ProtoCodec",new MessageCodec());

                            pipeline.addLast(new IdleStateHandler(0,5,0));
                            pipeline.addLast( new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

                                    IdleStateEvent event =  (IdleStateEvent)evt;
                                    if(event.state() == IdleState.WRITER_IDLE){
                                        log.info("检测到5秒未发数据发送心跳");
                                        PingMessage pingMessage = new PingMessage();
                                        pingMessage.setAlgorithm(rpcClientConfiguration.getSerializer());
                                        pingMessage.setSequenceId(UniqueIdGenerator.generateUniqueId());
                                        ctx.writeAndFlush(pingMessage);
                                    }
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    RpcHolder.removeChannelMapping(new ChannelMapping(serviceProvider.getServerIp(),serviceProvider.getRcpPort(), ctx.channel()));
                                    // 当连接断开时,执行此方法
                                    super.channelInactive(ctx);
                                }
                            });
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
                RpcHolder.set(request.getSequenceId(),requestPromise);
                RpcResponse rpcResponse = (RpcResponse) requestPromise.get();
                return rpcResponse;

        }catch (Exception e){
            e.printStackTrace();
        }
        return new RpcResponse();
    }
}
