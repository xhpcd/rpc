package com.xhpcd.rpc.server.boot.nett;

import com.xhpcd.rpc.common.IpUtils;
import com.xhpcd.rpc.handler.RpcRequestHandler;
import com.xhpcd.rpc.netty.codec.*;
import com.xhpcd.rpc.server.boot.RpcServer;
import com.xhpcd.rpc.server.boot.config.RpcServerConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
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
        /**
         * 一主多从的Reactor模式
         */
        NioEventLoopGroup boss = new NioEventLoopGroup(1, new DefaultThreadFactory("Boss"));
        NioEventLoopGroup worker = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        /**
         * 避免业务线程过多的占用Netty的线程
         */
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
                            //用来检测客户端是否假死
                            pipeline.addLast(new IdleStateHandler(20,0,0));
                            pipeline.addLast(new ChannelDuplexHandler(){

                                //即是入站也是出站处理器
                                //用来触发特定事件

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent idl = (IdleStateEvent)evt;
                                    if(idl.state() == IdleState.READER_IDLE){
                                        log.info("客户端10s未发送信息");
                                        ctx.channel().close();
                                    }
                                }
                            });
                            pipeline.addLast("FrameEncoder",new FrameEncoder());
                            pipeline.addLast("FrameDecoder",new FrameDecoder());
                            pipeline.addLast("ProtoCodec",new MessageCodec());
                          /*  pipeline.addLast("SerizableResponse",rpcResponseEncoder);
                            pipeline.addLast("DeSerializable",rpcRequestDecoder);*/
                            pipeline.addLast(eventExecutors,"rpcRequestHandler",rpcRequestHandler);

                        }
                    });
            InetSocketAddress inetSocketAddress = new InetSocketAddress(rpcServerConfiguration.getRpcPort());
            log.info("Server Netty bind {}:{}",inetSocketAddress.getAddress(),inetSocketAddress.getPort());
            ChannelFuture future = serverBootstrap.bind(inetSocketAddress).sync();

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
