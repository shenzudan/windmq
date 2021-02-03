package com.stanwind.wmqtt.beans;

import java.io.Serializable;

public abstract class MqttRequest implements Serializable {

    /**
     * 消息ID
     */
    protected long messageId;

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
