package com.xhpcd.rpc.client.spring;


import com.xhpcd.rpc.annotation.RpcRemote;
import com.xhpcd.rpc.client.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class RpcAnnotationProcessor implements BeanPostProcessor {
    @Autowired
    private ProxyFactory proxyFactory;
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcRemote annotation = declaredField.getAnnotation(RpcRemote.class);
            if(annotation != null){
                declaredField.setAccessible(true);
            }
            Class<?> type = declaredField.getType();
            Object o = proxyFactory.newProxyInstance(type);
            try {

                declaredField.set(bean,o);
            } catch (IllegalAccessException e) {
                log.error("filed {} inject field",declaredField);
                throw new RuntimeException(e);
            }
        }
        return bean;
    }
}
