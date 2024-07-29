package com.xhpcd.rpc.handler;

import com.xhpcd.rpc.data.RpcRequest;
import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.spring.SpringBeanFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            String className = rpcRequest.getClassName();
            String methodName = rpcRequest.getMethodName();
            Object[] parameters = rpcRequest.getParameters();
            Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端出现异常{}",cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }
}
