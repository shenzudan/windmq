package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.TopicPattern.TopicPatternDefinition;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * MsgHandlerDefinition 消息处理器定义
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:45
 **/
public class MsgHandlerDefinition implements Serializable {

    private TopicPatternDefinition patternDefinition;
    private String topic;
    private boolean full;
    private Class<?> claz;
    private Method method;

    public TopicPatternDefinition getPatternDefinition() {
        return patternDefinition;
    }

    public MsgHandlerDefinition setPatternDefinition(TopicPatternDefinition patternDefinition) {
        this.patternDefinition = patternDefinition;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public MsgHandlerDefinition setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public boolean isFull() {
        return full;
    }

    public MsgHandlerDefinition setFull(boolean full) {
        this.full = full;
        return this;
    }

    public Class<?> getClaz() {
        return claz;
    }

    public MsgHandlerDefinition setClaz(Class<?> claz) {
        this.claz = claz;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public MsgHandlerDefinition setMethod(Method method) {
        this.method = method;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MsgHandlerDefinition{");
        sb.append("patternDefinition=").append(patternDefinition);
        sb.append(", topic='").append(topic).append('\'');
        sb.append(", full=").append(full);
        sb.append(", claz=").append(claz);
        sb.append(", method=").append(method);
        sb.append('}');
        return sb.toString();
    }
}
