package com.xhpcd.rpc.netty.request;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;

public class RequestPromise extends DefaultPromise {
    public RequestPromise(EventExecutor eventExecutor){
        super(eventExecutor);
    }
}
