package com.xhpcd.rpc.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class Message {
    public Long sequenceId;
    public int messageType;
    public String algorithm;
    public static final int RpcRequest = 0;
    public static final int RpcResponse = 1;
    public abstract Integer getMessageType();
    public static final Map<Integer,Class<?>> messageClass = new HashMap<>();
    static {
        messageClass.put(0,com.xhpcd.rpc.data.RpcRequest.class);
        messageClass.put(1,com.xhpcd.rpc.data.RpcResponse.class);
    }
}
