package com.xhpcd.rpc.handler;

import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.netty.request.RequestPromise;
import com.xhpcd.rpc.util.RpcHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        RequestPromise requestPromise = RpcHolder.get(rpcResponse.getRequestId());
        requestPromise.setSuccess(rpcResponse);

    }
}
