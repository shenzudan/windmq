package com.stanwind.wmqtt.service;

import com.stanwind.wmqtt.message.MqttRequest;
import com.stanwind.wmqtt.message.MqttResponse;
import org.springframework.messaging.Message;

/**
 * IMessageService
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-10 19:53
 **/
public interface IMessageService {

    void notify(String deviceId, Object payload);

    void notifyToTopic(String topic, Object payload);

    MqttResponse request(String deviceId, MqttRequest payload);

    MqttResponse request(String deviceId, MqttRequest payload, long timeout);

    boolean response(Message<MqttResponse> message);

    /**
     * 客户端请求通用回复
     * @param messageId
     * @param deviceId
     * @param result
     */
    void sendCommonResponse(Long messageId, String deviceId, Integer result);
    void sendCommonResponse(Long messageId, String deviceId);
}
