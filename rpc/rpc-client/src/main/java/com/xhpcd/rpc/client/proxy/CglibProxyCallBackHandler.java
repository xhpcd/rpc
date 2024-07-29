package com.xhpcd.rpc.client.proxy;

import com.xhpcd.rpc.client.request.RpcRequestManager;
import com.xhpcd.rpc.data.RpcRequest;
import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.spring.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
public class CglibProxyCallBackHandler implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        log.info("method:{} 执行代理调用",method.getName());
        RpcRequest request = RpcRequest.builder().parameters(objects).parameterTypes(method.getParameterTypes())
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .requestId(UUID.randomUUID().toString()).build();

        RpcRequestManager rpcRequestManager = SpringBeanFactory.getBean(RpcRequestManager.class);
        RpcResponse response = rpcRequestManager.sendRequest(request);
        if(response.hasError()){
            throw response.getCause();
        }
        return response.getResult() ;

    }
}
