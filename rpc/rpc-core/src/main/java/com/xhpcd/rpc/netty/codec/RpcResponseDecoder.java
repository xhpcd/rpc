package com.xhpcd.rpc.netty.codec;

import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class RpcResponseDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);
        RpcResponse rpcResponse = ProtostuffUtil.deserializer(bytes, RpcResponse.class);
        list.add(rpcResponse);
    }
}
