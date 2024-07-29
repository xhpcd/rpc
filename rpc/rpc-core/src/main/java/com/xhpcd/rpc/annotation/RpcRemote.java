package com.xhpcd.rpc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcRemote {
    String value() default "";

    Class<?> interfaceClass() default void.class;




}
