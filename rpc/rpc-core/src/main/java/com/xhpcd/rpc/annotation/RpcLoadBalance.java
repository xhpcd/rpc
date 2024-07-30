package com.xhpcd.rpc.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 根据不同的策略选择不同的负载均衡实现
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcLoadBalance {
    @AliasFor(annotation = Component.class)
    String value() default "";

    String strategy() default "random";
}
