package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.TopicPattern;
import com.stanwind.wmqtt.handler.TopicPattern.TopicPatternDefinition;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * MQTTMsg 消息定义
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:42
 **/
public class MQTTMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    private TopicPatternDefinition patternDefinition;
    /* lazy parse*/
    private volatile Map<String, String> values;

    private String topic;
    private Object payload;

    public MQTTMsg(String topic, Object payload) {
        this(null, null, topic, payload);
    }

    public MQTTMsg(TopicPatternDefinition patternDefinition, Map<String, String> values, String topic,
            Object payload) {
        this.patternDefinition = patternDefinition;
        this.values = values;
        this.topic = topic;
        this.payload = payload;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public TopicPatternDefinition getPatternDefinition() {
        return patternDefinition;
    }

    public void setPatternDefinition(TopicPatternDefinition patternDefinition) {
        this.patternDefinition = patternDefinition;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Map<String, String> getValues() {
        if (Objects.isNull(values)) {
            synchronized (this) {
                if (Objects.isNull(values)) {
                    values = TopicPattern.getValueMap(topic, patternDefinition);
                }
            }
        }

        return values;
    }
}
