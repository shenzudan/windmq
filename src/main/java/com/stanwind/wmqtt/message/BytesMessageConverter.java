package com.stanwind.wmqtt.message;

import com.google.common.collect.ImmutableMap;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.mapping.BytesMessageMapper;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.mqtt.support.MqttMessageConverter;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.Assert;

/**
 * 自定义的消息转换器
 */
public class BytesMessageConverter implements MqttMessageConverter, BeanFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(BytesMessageConverter.class.getSimpleName());

    private final int defaultQos;

    private final MessageProcessor<Integer> qosProcessor;

    private final boolean defaultRetained;

    private final MessageProcessor<Boolean> retainedProcessor;

    private BytesMessageMapper bytesMessageMapper;

    private volatile BeanFactory beanFactory;

    private volatile MessageBuilderFactory messageBuilderFactory;

    public BytesMessageConverter(BytesMessageMapper bytesMessageMapper) {
        this.defaultQos = 0;
        this.qosProcessor = MqttMessageConverter.defaultQosProcessor();
        this.defaultRetained = false;
        this.retainedProcessor = MqttMessageConverter.defaultRetainedProcessor();
        this.bytesMessageMapper = bytesMessageMapper;
        this.messageBuilderFactory = new DefaultMessageBuilderFactory();
    }

    @Override
    public Message<?> toMessage(Object mqttMessage, MessageHeaders headers) {
        Assert.isInstanceOf(MqttMessage.class, mqttMessage,
                () -> "This converter can only convert an 'MqttMessage'; received: " + mqttMessage.getClass()
                        .getName());
        return toMessage(null, (MqttMessage) mqttMessage);
    }

    @Override
    public Message<?> toMessage(String topic, MqttMessage mqttMessage) {
        try {
            AbstractIntegrationMessageBuilder<?> messageBuilder;
            Message<?> message = bytesMessageMapper
                    .toMessage(mqttMessage.getPayload(), ImmutableMap.of(MqttHeaders.TOPIC, topic));

            messageBuilder = messageBuilderFactory.fromMessage(message);

            messageBuilder
                    .setHeader(MqttHeaders.RECEIVED_QOS, mqttMessage.getQos())
                    .setHeader(MqttHeaders.DUPLICATE, mqttMessage.isDuplicate())
                    .setHeader(MqttHeaders.RECEIVED_RETAINED, mqttMessage.isRetained())
                    .setHeader(MqttHeaders.RECEIVED_TOPIC, topic);

            return messageBuilder.build();
        } catch (Exception e) {
            logger.error("failed to convert object to Message", e);
            throw new MessageConversionException("failed to convert object to Message", e);
        }
    }

    @Override
    public Object fromMessage(Message<?> message, Class<?> aClass) {
        byte[] payloadBytes = messageToMqttBytes(message);
        MqttMessage mqttMessage = new MqttMessage(payloadBytes);
        Integer qos = this.qosProcessor.processMessage(message);
        mqttMessage.setQos(qos == null ? this.defaultQos : qos);
        Boolean retained = this.retainedProcessor.processMessage(message);
        mqttMessage.setRetained(retained == null ? this.defaultRetained : retained);
        return mqttMessage;
    }

    protected byte[] messageToMqttBytes(Message<?> message) {
        try {
            return this.bytesMessageMapper.fromMessage(message);
        } catch (Exception e) {
            logger.error("Failed to map outbound message", e);
            throw new MessageHandlingException(message, "Failed to map outbound message", e);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
