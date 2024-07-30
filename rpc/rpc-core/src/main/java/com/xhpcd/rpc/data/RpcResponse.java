package com.xhpcd.rpc.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

 /**
 *  远程调用请求的封装
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
