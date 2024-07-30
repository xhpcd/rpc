package com.xhpcd.rpc.netty.codec;

import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 作为协议的编码
 */
public class FrameEncoder extends LengthFieldPrepender {
    public FrameEncoder() {
        super(4);
    }
}
