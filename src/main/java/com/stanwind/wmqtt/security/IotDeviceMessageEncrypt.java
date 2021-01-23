package com.stanwind.wmqtt.security;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

/**
 * IotDeviceMessageEncrypt
 * IOT设备通信
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 16:17
 **/
public abstract class IotDeviceMessageEncrypt implements IMsgEncrypt {
    /**
     * IOT开头的Topic需要加解密消息
     * @param message
     * @return
     */
    @Override
    public boolean isEncrypt(Message<?> message) {
        String topic = message.getHeaders().get(MqttHeaders.TOPIC, String.class);
        return isEncrypt(topic);
    }

    @Override
    public boolean isEncrypt(String topic) {
        return !StringUtils.isEmpty(topic) && topic.startsWith("IOT") && !topic.endsWith("_FRONT");
    }
}
