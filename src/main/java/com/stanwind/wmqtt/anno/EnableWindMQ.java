package com.stanwind.wmqtt.anno;

import java.lang.annotation.*;
import org.springframework.context.annotation.Import;

/**
 * EnableWindMQ auto config
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2021-02-03 11:54 AM
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(WindMQAutoConfiguration.class)
public @interface EnableWindMQ {

}
