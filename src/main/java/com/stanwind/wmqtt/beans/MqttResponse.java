package com.stanwind.wmqtt.beans;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class MqttResponse implements Serializable {

    /**
     * 应答消息ID
     */
    protected long messageId;

    /**
     * 设备ID
     */
    protected String deviceId;
}
