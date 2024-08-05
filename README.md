使用篇

# rpc模块

rpc模块作为整个项目的核心，核心功能都在这个模块内

##     rpc-core

​          rpc-core模块作为rpc功能实现的核心，包括一些工具类，编解码器等作为rpc-client 和 rpc-server共同依赖部分

##     rpc-client

​           rpc-client为rpc框架的consumer端提供了服务发现，服务订阅，代理生成，远程通信等 功能实现，服被rpc-demo模块的service-consuer模块引用

##     rpc-server

​           rpc-server模块为rpc框架的provider端提供了服务注册，服务订阅，远程通信等 功能实现，服被rpc-demo模块的service-proovider模块引用



#   rpc-demo模块：

用于提供rpc框架功能演示

##      api模块：作为远程调用的通用接口模块

##      service-consumer模块：作为rpc的消费端，需要引入rpc-client模块

​                 

```
@RestController
@RequestMapping("/order")
public class OrderController {


    @RpcRemote
    private OrderService orderService;


    @GetMapping("/getOrder")
    public String getOrder(String userId,String orderId){
        return orderService.getOrder(userId,orderId);
    }

}
```

​              @RpcRemote注解标识要生成的代理对象 



```
spring:
  application:
    name: consumer

server:
  port: 8088

#配置服务的注册中心以及timeout等信息
rpc:
  client:
    zk:
      root: /rpc
      addr: 192.168.88.130:2181
      timeout: 10000
#指定集群选择使用的负载均衡算法
  cluster:
    strategy: polling
#指定客户端消息使用的序列化方式
  serializer: PROTOBUF
```



##       service-provider模块：rpc服务的提供方，需要引入rpc-server模块

​           

```
@RpcService(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Override
    public String getOrder(String userId, String orderId) {
        return return "provider receive userId: "+userId+" orderId: "+orderId;
    }
}
```

@RpcService注解标识要提供服务的实现类，interfaceClass标识要导出的服务接口

```
spring:
  application:
    name: service-provider

server:
  port: 8082

#设置远程同时暴露的端口
rpc:
  network:
    port: 9000
#注册中心配置    
  server:
    zk:
      root: /rpc
      addr: 192.168.88.130:2181
      timeout: 10000
```

效果演示

![image-20240805145115407](C:\Users\Sternstunde\AppData\Roaming\Typora\typora-user-images\image-20240805145115407.png)





# 原理分析

##     自定义协议格式

​            

```
/*
+---------------------------------------------------------------+
| 魔数 5byte | 协议版本号 1byte | 序列化算法 1byte | 指令类型 1byte|
+---------------------------------------------------------------+
|    请求序号 8byte                  |      数据长度 4byte        | 
+---------------------------------------------------------------+
| 填充字段 12byte       |   数据内容 （长度不定）                  |
+---------------------------------------------------------------+
*/
```

##     协议编解码器

​    为了解决粘包半包问题在协议编解码器之前还有LengthField编解码器

​     

```
public class MessageCodec extends ByteToMessageCodec<Message> {
    static final byte[] MAGIC_NUMBER = { 'x', 'h', 'p', 'c', 'd'};
    static final byte version = 1;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        //魔数  5
        byteBuf.writeBytes(MAGIC_NUMBER);
        //版本号
        byteBuf.writeByte(version);
        //序列化算法 0 jdk 1 json 2 proto  1
        int ordinal = Serializer.Algorithm.valueOf(message.getAlgorithm()).ordinal();
        byteBuf.writeByte(ordinal);
        //指令类型 登录 注册 退出等   1
        byteBuf.writeByte(message.getMessageType());
        //请求序号 实现异步线程间通信  8
        byteBuf.writeLong(message.getSequenceId());
        //正文长度
        //正文内容
        Serializer.Algorithm algorithm = Serializer.Algorithm.valueOf(message.getAlgorithm());
        byte[] serialize = algorithm.serialize(message);
        byteBuf.writeInt(serialize.length); //4
        byte[] paddingBytes = new byte[12];
        byteBuf.writeBytes(paddingBytes); //5+1+1+1+8+4+12 = 32 2的整数次方
        byteBuf.writeBytes(serialize);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] magicNum = new byte[5];
        byteBuf.readBytes(magicNum, 0, 5);
        boolean isMagicNumberMatch = Arrays.equals(magicNum, MAGIC_NUMBER);
        if(!isMagicNumberMatch){
            ChannelFuture future = channelHandlerContext.channel().close();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        log.info("协议异常断开连接");
                    }else {
                        log.info("连接断开发生错误请检查");
                    }
                }
            });
            return;
        }
        byte version = byteBuf.readByte();
        byte serializer = byteBuf.readByte();
        byte type = byteBuf.readByte();
        Long SequenceId = byteBuf.readLong();
        int length = byteBuf.readInt();
        byteBuf.readBytes(new byte[12],0,12);
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes,0,length);
        Object o = null;
        Serializer.Algorithm[] values = Serializer.Algorithm.values();
        o = values[serializer].deserialize(Message.messageClass.get((int)type),bytes);
        list.add(o);
        log.info("长度{}",length);

    }
```

请求体响应体共有信息的抽取

```
public abstract class Message {
    public Long sequenceId;
    public int messageType;
    public String algorithm;
    public static final int RpcRequest = 0;
    public static final int RpcResponse = 1;
    public static final int PingMessage = 2;
    public abstract Integer getMessageType();
    public static final Map<Integer,Class<?>> messageClass = new HashMap<>();
    static {
        messageClass.put(0,com.xhpcd.rpc.data.RpcRequest.class);
        messageClass.put(1,com.xhpcd.rpc.data.RpcResponse.class);
        messageClass.put(2,com.xhpcd.rpc.data.PingMessage.class);
    }
}
```



携带参数等信息的 请求体

```
public class RpcRequest extends Message{
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    @Override
    public Integer getMessageType() {
        return RpcRequest;
    }
}
```

结果响应的消息体

```
public class RpcResponse extends Message {
    private Object result;
    private Throwable cause;

    public boolean hasError(){
        return cause!=null;
    }

     @Override
     public Integer getMessageType() {
         return RpcResponse;
     }
 }
```



## 服务注册

​    @RpcService注解 包含了@Component注解，标记的实现类会被spring注册到容器中

```
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {
    /**
     * @Component 的value
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 服务接口的class
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务接口名称
     * @return
     */
    String interfaceName() default "";

    /**
     * 服务版本号
     * @return
     */
    String version() default "";

    /**
     * 服务的权重
     */
    int weight() default 1;
}
```

向zookeeper注册信息并启动Netty

```
@Override
public void serviceRegistry() {
    Map<String, Object> beanByAnnotation = SpringBeanFactory.getBeanByAnnotation(RpcService.class);
    if(beanByAnnotation!=null&&!beanByAnnotation.isEmpty()) {
        //根节点的创建
        serverZKit.createRootNode();
        //ip获取
        String serverIp = IpUtils.getRealIp();
        for (Map.Entry<String, Object> entry : beanByAnnotation.entrySet()) {
            RpcService annotation = entry.getValue().getClass().getAnnotation(RpcService.class);
            Class<?> interfaceClass = annotation.interfaceClass();
            int weight = annotation.weight();
            //服务名称
            String name = interfaceClass.getName();
            serverZKit.createPersistentNode(name);
            String providerNode = serverIp+":"+rpcServerConfiguration.getRpcPort()+":"+weight;
            serverZKit.createNode(name+"/"+providerNode);
            log.info("服务{}-{}完成了注册",name,providerNode);
        }
        rpcServer.start();
    }
}
```

provider Netty 详细信息



```
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
                                    log.info("客户端15s未发送信息");
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
```

重点在于服务提供者的执行

pipeline.addLast(eventExecutors,"rpcRequestHandler",rpcRequestHandler);

```
protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

    RpcResponse rpcResponse = new RpcResponse();
    rpcResponse.setSequenceId(rpcRequest.sequenceId);
    rpcResponse.setAlgorithm(rpcRequest.getAlgorithm());
    rpcResponse.setMessageType(Message.RpcResponse);
    try {
        String className = rpcRequest.getClassName();
        String methodName = rpcRequest.getMethodName();
        Object[] parameters = rpcRequest.getParameters();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        //通过spring容器获取实现类
        Object bean = SpringBeanFactory.getBean(Class.forName(className));
        Method method = bean.getClass().getMethod(methodName, parameterTypes);
        Object result = method.invoke(bean,parameters);
        rpcResponse.setResult(result);
    } catch (Exception e){
        rpcResponse.setCause(e);
        log.error("RpcRequestHandler service has error");
    }finally {
        channelHandlerContext.channel().writeAndFlush(rpcResponse);
    }
}
```

通过spring容器来获得对应的实现类，并通过反射调用其实现方法，并把结果和异常（如果存在）封装为Response返回





# 服务发现，代理生成，字段注入，服务订阅



## 服务发现

```
public void rpcServerDiscovery() {

    List<String> service = clientZKit.getService();
    for (String s : service) {
        List<ServiceProvider> serviceInfos = clientZKit.getServiceInfos(s);
        serviceProviderCache.put(s,serviceInfos);
        clientZKit.subscribeZKNode(s);
        log.info("client subscribe {},services{}",s,serviceInfos);
    }
}
```



通过获得节点信息拿到所有服务，进一步拿到每一个服务的提供者信息封装为Serviceprovider并缓存，同时监听节点发生变化，并及时更新缓存



## 代理生成，字段注入

利用spring的生命周期使用BeanPostProcessor对每个bean进行后置处理

检测所有带@RpcRemote的注解，基于继承的方式生成代理对象并注入。

```
@Override
public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> aClass = bean.getClass();
    Field[] declaredFields = aClass.getDeclaredFields();
    for (Field declaredField : declaredFields) {
        RpcRemote annotation = declaredField.getAnnotation(RpcRemote.class);
        if(annotation != null){
            declaredField.setAccessible(true);
            Class<?> type = declaredField.getType();
            Object o = proxyFactory.newProxyInstance(type);
            try {

                declaredField.set(bean,o);
            } catch (IllegalAccessException e) {
                log.error("filed {} inject field",declaredField);
                 throw new RuntimeException(e);
            }
        }

    }
    return bean;
}
```

代理内通过封装请求消息体通过Netty完成消息发送      RpcResponse response = rpcRequestManager.sendRequest(request);

```
public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

    log.info("method:{} 执行代理调用",method.getName());
    RpcClientConfiguration clientConfiguration = SpringBeanFactory.getBean(RpcClientConfiguration.class);
    RpcRequest request = RpcRequest.builder().parameters(objects).parameterTypes(method.getParameterTypes())
            .className(method.getDeclaringClass().getName())
            .methodName(method.getName())
            .build();
    request.setSequenceId(UniqueIdGenerator.generateUniqueId());
    request.setMessageType(Message.RpcRequest);
    request.setAlgorithm(clientConfiguration.getSerializer());

    RpcRequestManager rpcRequestManager = SpringBeanFactory.getBean(RpcRequestManager.class);
    RpcResponse response = rpcRequestManager.sendRequest(request);
    if(response.hasError()){
        throw response.getCause();
    }
    return response.getResult() ;

}
```

**负载均衡算法的选择**

```
public RpcResponse sendRequest(RpcRequest request) {
    List<ServiceProvider> serviceProviders = serviceProviderCache.get(request.getClassName());
    LoadBalanceStrategy strategy = strategyProvider.getStrategy();
    ServiceProvider serviceProvider = strategy.select(serviceProviders);
    return doSendRequest(request,serviceProvider);
}
```

  

进行Channel的建立和缓存先查询是否存在相应的channel，如果存在就可以复用

```
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

                    pipeline.addLast(new IdleStateHandler(0,15,0));
                    pipeline.addLast( new ChannelDuplexHandler(){
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

                            IdleStateEvent event =  (IdleStateEvent)evt;
                            if(event.state() == IdleState.WRITER_IDLE){
                                log.info("检测到15秒未发数据发送心跳");
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
```

根据请求消息中SequenceId生成一个promise，把id和promise通过map缓存，来进行用户线程和netty线程的结果通信



对于Netty来说，把结果根据ID查找到对应的promise

```
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        //线程间同步结果共享
        RequestPromise requestPromise = RpcHolder.get(rpcResponse.getSequenceId());
        requestPromise.setSuccess(rpcResponse);

    }
}
```

# 心跳检测

服务端通过空闲检测来判断是否客户端已经断开



```
pipeline.addLast(new IdleStateHandler(20,0,0));
pipeline.addLast(new ChannelDuplexHandler(){

    //即是入站也是出站处理器
    //用来触发特定事件

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent idl = (IdleStateEvent)evt;
        if(idl.state() == IdleState.READER_IDLE){
            log.info("客户端15s未发送信息");
            ctx.channel().close();
        }
    }
});
```

客户端通过定时任务发送心跳信息

```
pipeline.addLast(new IdleStateHandler(0,15,0));
pipeline.addLast( new ChannelDuplexHandler(){
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        IdleStateEvent event =  (IdleStateEvent)evt;
        if(event.state() == IdleState.WRITER_IDLE){
            log.info("检测到15秒未发数据发送心跳");
            PingMessage pingMessage = new PingMessage();
            pingMessage.setAlgorithm(rpcClientConfiguration.getSerializer());
            pingMessage.setSequenceId(UniqueIdGenerator.generateUniqueId());
            ctx.writeAndFlush(pingMessage);
        }
    }
```
