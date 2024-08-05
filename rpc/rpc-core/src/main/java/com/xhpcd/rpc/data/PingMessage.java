package com.xhpcd.rpc.data;

import lombok.Data;

@Data
public class PingMessage extends Message{
    @Override
    public Integer getMessageType() {
        return PingMessage;
    }
}
