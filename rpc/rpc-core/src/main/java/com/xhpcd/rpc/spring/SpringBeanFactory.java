package com.xhpcd.rpc.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

@Component("springBeanFactory")
public class SpringBeanFactory implements ApplicationContextAware {
    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public static <T> T getBean(Class<T> cls){
        return context.getBean(cls);
    }

    public static Object getBean(String name){
        return context.getBean(name);
    }

    public static Map<String,Object> getBeanByAnnotation(Class<? extends Annotation> annotaionClass){
        return context.getBeansWithAnnotation(annotaionClass);
    }
}
