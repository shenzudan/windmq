package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.TopicPattern;
import com.stanwind.wmqtt.handler.TopicPattern.TopicPatternDefinition;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * MQTTMsg 消息定义
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:42
 **/
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Data
public class MQTTMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    private TopicPatternDefinition patternDefinition;
    /* lazy parse*/
    private volatile Map<String, String> values;

    private String topic;
    private Object payload;

    public MQTTMsg(String topic, Object payload) {
        this.topic = topic;
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
