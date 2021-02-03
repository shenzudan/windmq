package com.stanwind.wmqtt.beans;

import com.stanwind.wmqtt.anno.Topic;

/**
 * 通用回复
 */
@Topic("IOT_SERVER/reply/{deviceId}")
public class CommonResponse extends MqttResponse {

    /**
     * 0.成功
     * 1.失败
     * 2.消息错误
     * 3.不支持
     */
    private byte result;

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public CommonResponse() {
    }

    public CommonResponse(long messageId, String deviceId, byte result) {
        super(messageId, deviceId);
        this.result = result;
    }
}
