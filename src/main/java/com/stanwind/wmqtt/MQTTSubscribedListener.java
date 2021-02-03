package com.stanwind.wmqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
import org.springframework.stereotype.Component;

/**
 * MQTTSubscribedListener 绑定监听器
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-16 17:50
 **/
@Component
public class MQTTSubscribedListener implements ApplicationListener<MqttSubscribedEvent> {

    private static final Logger log = LoggerFactory.getLogger(MQTTSubscribedListener.class);

    @Override
    public void onApplicationEvent(MqttSubscribedEvent event) {
        log.debug("Subscribed Success: " + event.getMessage());
    }
}
