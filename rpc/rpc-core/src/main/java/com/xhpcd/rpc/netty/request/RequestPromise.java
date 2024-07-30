package com.xhpcd.rpc.netty.request;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;

/**
 * 用于和Netty的线程通信
 */
public class RequestPromise extends DefaultPromise {
    public RequestPromise(EventExecutor eventExecutor){
        super(eventExecutor);
    }
}
