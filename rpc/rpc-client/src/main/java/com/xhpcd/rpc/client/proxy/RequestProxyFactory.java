package com.xhpcd.rpc.client.proxy;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Component;

@Component
public class RequestProxyFactory implements ProxyFactory{
    @Override
    public <T> T newProxyInstance(Class<T> cls) {
       Enhancer enhancer = new Enhancer();
       enhancer.setSuperclass(cls);
       enhancer.setCallback(new CglibProxyCallBackHandler());
       return (T)enhancer.create();
    }
}
