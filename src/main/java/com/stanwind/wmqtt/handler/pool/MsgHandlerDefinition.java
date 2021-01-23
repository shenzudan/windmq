package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.TopicPattern.TopicPatternDefinition;
import java.io.Serializable;
import java.lang.reflect.Method;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * MsgHandlerDefinition 消息处理器定义
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:45
 **/
@Data
@Accessors(chain = true)
public class MsgHandlerDefinition implements Serializable {
    private TopicPatternDefinition patternDefinition;
    private String topic;
    private boolean full;
    private Class<?> claz;
    private Method method;
}
