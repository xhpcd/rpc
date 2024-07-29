package com.xhpcd.rpc.server.boot.nett;

import com.xhpcd.rpc.handler.RpcRequestHandler;
import com.xhpcd.rpc.netty.codec.FrameDecoder;
import com.xhpcd.rpc.netty.codec.FrameEncoder;
import com.xhpcd.rpc.netty.codec.RpcRequestDecoder;
import com.xhpcd.rpc.netty.codec.RpcResponseEncoder;
import com.xhpcd.rpc.server.boot.RpcServer;
import com.xhpcd.rpc.server.boot.config.RpcServerConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.net.InetSocketAddress;
@Slf4j
@Component
public class NettyServer implements RpcServer, Serializable {
    @Autowired
    private RpcServerConfiguration rpcServerConfiguration;
    @Override
    public void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup(1, new DefaultThreadFactory("Boss"));
        NioEventLoopGroup worker = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        UnorderedThreadPoolEventExecutor eventExecutors = new UnorderedThreadPoolEventExecutor(NettyRuntime.availableProcessors() * 2);

        try {
            RpcRequestDecoder rpcRequestDecoder = new RpcRequestDecoder();
            RpcResponseEncoder rpcResponseEncoder = new RpcResponseEncoder();
            RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("FrameEncoder",new FrameEncoder());
                            pipeline.addLast("SerizableResponse",rpcResponseEncoder);
                            pipeline.addLast("FrameDecoder",new FrameDecoder());
                            pipeline.addLast("DeSerializable",rpcRequestDecoder);
                            pipeline.addLast(eventExecutors,"rpcRequestHandler",rpcRequestHandler);

                        }
                    });
            ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(rpcServerConfiguration.getRpcPort())).sync();

            future.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                    eventExecutors.shutdownGracefully();
                }
            });
        }catch (Exception e){
            log.error("Netty bind hava error");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            eventExecutors.shutdownGracefully();
        }
    }
}
