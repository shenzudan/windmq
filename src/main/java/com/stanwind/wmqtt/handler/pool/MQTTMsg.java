package com.stanwind.wmqtt.handler.pool;

import java.io.Serializable;
import java.util.Map;
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
    private Map<String, String> values;
    private String topic;
    private Object payload;

    public MQTTMsg(String topic, Object payload) {
        this.topic = topic;
        this.payload = payload;
    }
}
