package com.xhpcd.rpc.netty.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class FrameDecoder extends LengthFieldBasedFrameDecoder {
    public FrameDecoder() {
        super(Integer.MAX_VALUE, 0, 4,0,4);
    }
}
