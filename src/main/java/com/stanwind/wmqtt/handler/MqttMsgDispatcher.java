package com.stanwind.wmqtt.handler;

import com.alibaba.fastjson.JSONObject;
import com.stanwind.wmqtt.MqttConfig;
import com.stanwind.wmqtt.handler.pool.MQTTMsg;
import com.stanwind.wmqtt.handler.pool.MsgAdapter;
import com.stanwind.wmqtt.message.MqttResponse;
import com.stanwind.wmqtt.service.IMessageService;
import com.stanwind.wmqtt.utils.Tools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

/**
 * MsgReceiveHandler
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-10 19:55
 **/
@Service
@Slf4j
public class MqttMsgDispatcher implements MessageHandler {

    @Autowired
    private IMessageService messageService;

    @ServiceActivator(inputChannel = MqttConfig.CHANNEL_NAME_IN)
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        Object payload = message.getPayload();
        log.debug("\r\n--------------Rec---------------\r\nTopic:\t" + topic + "\r\nData:\t" + payload
                + "\r\n--------------------------------");

        try {
            //需要响应的同步类消息 做消息确认
            if (payload instanceof MqttResponse) {
                messageService.response((Message<MqttResponse>) message);
            }

            if (MsgAdapter.isContainsFullTopic(topic)) {
                MsgAdapter adapter = MsgAdapter.getFullTopicAdapter(topic);
                if (adapter != null) {
                    adapter.processMsg(new MQTTMsg(topic, payload));
                } else {
                    log.warn("full adapter not exist: " + topic + " data->" + payload);
                }
                return;
            }

            String fuzzyTopic = Tools.fuzzyTopic(topic);
            if (MsgAdapter.isContainsFuzzyTopic(fuzzyTopic)) {
                MsgAdapter adapter = MsgAdapter.getFuzzyTopicAdapter(fuzzyTopic);
                if (adapter != null) {
                    adapter.processMsg(new MQTTMsg(topic, payload));
                } else {
                    log.warn("fuzzy adapter not exist: " + topic + " data->" + payload);
                }

                return;
            }

            {
                //遍历匹配开始结束
                MsgAdapter adapter = MsgAdapter.transRegAdapter(topic);
                if (adapter != null) {
                    adapter.processMsg(new MQTTMsg(TopicPattern.getValueMap(topic, adapter.getHandler()
                            .getPatternDefinition()), topic, payload));
                    return;
                }
            }

            log.warn("\r\n-----adapter not exist-------\r\nTopic:\t" + topic + "\r\nData:\t" + payload
                    + "\r\n-----------------------------");
//            if (topic.startsWith("iot_client")) {
//                String deviceId = "test123";
//                MqttRequest request = JsonUtil.parseObject((String) payload, SettingUpdate.class);
//                CommonResponse commonResponse = new CommonResponse(request.getMessageId(), deviceId, (byte) 0);
//                messageService.notify(deviceId, commonResponse);
//            }
        } catch (Exception e) {
            log.error("\n系统出错: " + JSONObject.toJSONString(payload), e);
        }
    }
}
