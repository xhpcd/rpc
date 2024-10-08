package com.xhpcd.rpc.netty.codec;

import com.xhpcd.rpc.data.RpcRequest;
import com.xhpcd.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * 数据的解码 使用protobuf进行序列化反序列化
 */
public class RpcRequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int i = byteBuf.readableBytes();
        byte[] bytes = new byte[i];
        byteBuf.readBytes(bytes);
        RpcRequest deserializer = ProtostuffUtil.deserializer(bytes, RpcRequest.class);
        list.add(deserializer);
    }
}
