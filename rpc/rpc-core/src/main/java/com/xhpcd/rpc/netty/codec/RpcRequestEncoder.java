package com.xhpcd.rpc.netty.codec;

import com.xhpcd.rpc.data.RpcRequest;
import com.xhpcd.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;


/**
 * 数据的编码 使用protobuf进行序列化序列化
 */
public class RpcRequestEncoder extends MessageToMessageEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, List<Object> list) throws Exception {
        byte[] bytes = ProtostuffUtil.serializer(rpcRequest);
        ByteBuf buffer = channelHandlerContext.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);
        list.add(buffer);
    }
}
