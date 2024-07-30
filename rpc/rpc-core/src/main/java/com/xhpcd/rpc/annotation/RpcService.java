package com.xhpcd.rpc.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 用于服务注册的接口 同时把实现类交由容器管理
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {
    /**
     * @Component 的value
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 服务接口的class
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务接口名称
     * @return
     */
    String interfaceName() default "";

    /**
     * 服务版本号
     * @return
     */
    String version() default "";

    /**
     * 服务的权重
     */
    int weight() default 1;
}
