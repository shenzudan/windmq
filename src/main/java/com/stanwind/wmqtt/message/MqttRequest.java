package com.stanwind.wmqtt.message;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class MqttRequest implements Serializable {

    /**
     * 消息ID
     */
    protected long messageId;
}
