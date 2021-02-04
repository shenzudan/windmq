package com.stanwind.wmqtt.anno;

import java.lang.annotation.*;

/**
 * topic bean anno
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Topic {
    String value();
}
