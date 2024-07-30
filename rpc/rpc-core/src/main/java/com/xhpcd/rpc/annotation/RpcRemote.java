package com.xhpcd.rpc.annotation;

import java.lang.annotation.*;

/**
 * 作为代理注入的注解
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcRemote {
    String value() default "";

    Class<?> interfaceClass() default void.class;

}
