package com.stanwind.wmqtt.anno;

import java.lang.annotation.*;

/**
 * TopicHandler Topic处理注解
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 18:30
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TopicHandler {
    String topic();

    /**
     * 如果为full则认为topic是完整路径
     * @return
     */
    boolean full() default false;
}
