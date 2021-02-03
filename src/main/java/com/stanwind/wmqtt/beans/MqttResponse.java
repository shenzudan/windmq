package com.stanwind.wmqtt.beans;

import java.io.Serializable;

public abstract class MqttResponse implements Serializable {

    /**
     * 应答消息ID
     */
    protected long messageId;

    /**
     * 设备ID
     */
    protected String deviceId;


    public MqttResponse(long messageId, String deviceId) {
        this.messageId = messageId;
        this.deviceId = deviceId;
    }

    public MqttResponse() {
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
