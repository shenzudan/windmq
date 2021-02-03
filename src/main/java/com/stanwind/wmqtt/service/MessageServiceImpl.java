package com.stanwind.wmqtt.service;

import com.stanwind.wmqtt.anno.Topic;
import com.stanwind.wmqtt.beans.CommonResponse;
import com.stanwind.wmqtt.beans.MqttRequest;
import com.stanwind.wmqtt.beans.MqttResponse;
import com.stanwind.wmqtt.handler.MqttSender;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.RendezvousChannel;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * MessageServiceImpl
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-10 19:53
 **/
@Service
public class MessageServiceImpl implements IMessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private Map<String, RendezvousChannel> topicSubscribers = new ConcurrentHashMap<>();

    @Autowired
    private MqttSender mqttSender;

    @Override
    public void notify(String deviceId, Object payload) {
        Topic annotation = payload.getClass().getAnnotation(Topic.class);
        String value = annotation.value();
        String topic = value.replace("{deviceId}", deviceId);
        log.debug("send: t: {} p: {}", topic, payload);
        notifyToTopic(topic, payload);
    }

    @Override
    public void notifyToTopic(String topic, Object payload) {
        log.debug("send: t: {} p: {}", topic, payload);
        mqttSender.send(topic, payload);
    }

    @Override
    public MqttResponse request(String deviceId, MqttRequest payload) {
        return request(deviceId, payload, 10000);
    }

    @Override
    public MqttResponse request(String deviceId, MqttRequest payload, long timeout) {
        Class<? extends MqttRequest> clazz = payload.getClass();
        Topic annotation = clazz.getAnnotation(Topic.class);
        String value = annotation.value();
        String topic = value.replace("{deviceId}", deviceId);

        long messageId = payload.getMessageId();
        if (messageId == 0L) {
            payload.setMessageId(messageId = System.currentTimeMillis());
        }

        String key = getKey(deviceId, messageId);
        RendezvousChannel responseChannel = subscribe(key);
        if (responseChannel == null) {
            return null;
        }

        try {
            mqttSender.send(topic, payload);
            Message<MqttResponse> response = (Message<MqttResponse>) responseChannel.receive(timeout);
            if (response != null) {
                return response.getPayload();
            }
        } finally {
            unSubscribe(key);
        }
        return null;
    }

    @Override
    public boolean response(Message<MqttResponse> message) {
        MqttResponse payload = message.getPayload();
        RendezvousChannel responseChannel = topicSubscribers.get(getKey(payload.getDeviceId(), payload.getMessageId()));
        if (responseChannel != null) {
            return responseChannel.send(message);
        }
        return false;
    }

    private String getKey(String deviceId, long messageId) {
        return deviceId + "/" + messageId;
    }

    private RendezvousChannel subscribe(String key) {
        RendezvousChannel result = null;
        if (!topicSubscribers.containsKey(key)) {
            topicSubscribers.put(key, result = new RendezvousChannel());
        }
        return result;
    }

    private void unSubscribe(String key) {
        topicSubscribers.remove(key);
    }

    @Override
    public void sendCommonResponse(Long messageId, String deviceId, Integer result) {
        CommonResponse commonResponse = new CommonResponse(messageId, deviceId, result.byteValue());
        notify(deviceId, commonResponse);
    }

    @Override
    public void sendCommonResponse(Long messageId, String deviceId) {
        sendCommonResponse(messageId, deviceId, 0);
    }
}
