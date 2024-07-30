package com.xhpcd.rpc.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 远程调用请求的封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
