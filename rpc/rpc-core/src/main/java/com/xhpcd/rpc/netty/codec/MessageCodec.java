package com.xhpcd.rpc.netty.codec;


import com.xhpcd.rpc.common.Serializer;
import com.xhpcd.rpc.data.Message;
import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.spring.factorybean.SpringBeanFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 喜欢排长队
 * @Date: 2023/12/09/22:18
 * @Description:保持2的正数次方
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    static final byte[] MAGIC_NUMBER = { 'x', 'h', 'p', 'c', 'd'};
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        //魔数
        byteBuf.writeBytes(MAGIC_NUMBER);
        //序列化算法 0 jdk 1 json 2 proto
        int ordinal = Serializer.Algorithm.valueOf(message.getAlgorithm()).ordinal();
        byteBuf.writeByte(ordinal);
        //指令类型 登录 注册 退出等
        byteBuf.writeByte(message.getMessageType());
        //请求序号 实现异步全双工
        byteBuf.writeLong(message.getSequenceId());
        //正文长度
        //正文内容
        Serializer.Algorithm algorithm = Serializer.Algorithm.valueOf(message.getAlgorithm());
        byte[] serialize = algorithm.serialize(message);
        byteBuf.writeInt(serialize.length);
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
        byte serializer = byteBuf.readByte();
        byte type = byteBuf.readByte();
        Long SequenceId = byteBuf.readLong();
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes,0,length);
        Object o = null;
        Serializer.Algorithm[] values = Serializer.Algorithm.values();
        o = values[serializer].deserialize(Message.messageClass.get((int)type),bytes);
        list.add(o);
        log.info("长度{}",length);

    }
}
