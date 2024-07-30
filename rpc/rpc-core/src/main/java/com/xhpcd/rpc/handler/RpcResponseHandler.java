package com.xhpcd.rpc.handler;

import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.netty.request.RequestPromise;
import com.xhpcd.rpc.util.RpcHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        //线程间同步结果共享
        RequestPromise requestPromise = RpcHolder.get(rpcResponse.getSequenceId());
        requestPromise.setSuccess(rpcResponse);

    }
}
