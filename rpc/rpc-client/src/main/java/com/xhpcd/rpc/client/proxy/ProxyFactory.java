package com.xhpcd.rpc.client.proxy;

public interface ProxyFactory {
    <T> T newProxyInstance(Class<T> cls);
}
