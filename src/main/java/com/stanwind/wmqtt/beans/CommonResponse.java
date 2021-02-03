package com.stanwind.wmqtt.beans;

import com.stanwind.wmqtt.anno.Topic;
import lombok.Data;

/**
 * 通用回复
 */
@Topic("IOT_SERVER/reply/{deviceId}")
@Data
public class CommonResponse extends MqttResponse {

    /**
     * 0.成功
     * 1.失败
     * 2.消息错误
     * 3.不支持
     */
    private byte result;

    public CommonResponse() {
    }

    public CommonResponse(long messageId, String deviceId, byte result) {
        super(messageId, deviceId);
        this.result = result;
    }
}
