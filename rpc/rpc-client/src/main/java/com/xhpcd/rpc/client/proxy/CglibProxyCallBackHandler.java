package com.xhpcd.rpc.client.proxy;

import com.xhpcd.rpc.client.config.RpcClientConfiguration;
import com.xhpcd.rpc.client.request.RpcRequestManager;
import com.xhpcd.rpc.data.Message;
import com.xhpcd.rpc.data.RpcRequest;
import com.xhpcd.rpc.data.RpcResponse;
import com.xhpcd.rpc.spring.factorybean.SpringBeanFactory;
import com.xhpcd.rpc.util.UniqueIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;


@Slf4j
public class CglibProxyCallBackHandler implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        log.info("method:{} 执行代理调用",method.getName());
        RpcClientConfiguration clientConfiguration = SpringBeanFactory.getBean(RpcClientConfiguration.class);
        RpcRequest request = RpcRequest.builder().parameters(objects).parameterTypes(method.getParameterTypes())
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .build();
        request.setSequenceId(UniqueIdGenerator.generateUniqueId());
        request.setMessageType(Message.RpcRequest);
        request.setAlgorithm(clientConfiguration.getSerializer());

        RpcRequestManager rpcRequestManager = SpringBeanFactory.getBean(RpcRequestManager.class);
        RpcResponse response = rpcRequestManager.sendRequest(request);
        if(response.hasError()){
            throw response.getCause();
        }
        return response.getResult() ;

    }
}
