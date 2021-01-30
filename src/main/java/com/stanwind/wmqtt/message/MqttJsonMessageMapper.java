package com.stanwind.wmqtt.message;

import com.alibaba.fastjson.JSONObject;
import com.stanwind.wmqtt.anno.Topic;
import com.stanwind.wmqtt.security.IMsgEncrypt;
import com.stanwind.wmqtt.utils.ClassUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mapping.BytesMessageMapper;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * MqttJsonMessageMapper 使用Json传输报文(基于Jackson)
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-10 19:42
 **/
@Slf4j
public class MqttJsonMessageMapper implements BytesMessageMapper {

    private static Map<String, Class> mapper = new HashMap<>();

    private IMsgEncrypt encrypt;

    public MqttJsonMessageMapper(String modelPackages, IMsgEncrypt encrypt) {
        this(modelPackages);
        this.encrypt = encrypt;
    }

    public MqttJsonMessageMapper(String modelPackages) {
        String[] packages = modelPackages.split(",");
        for (String aPackage : packages) {
            List<Class<?>> classList = ClassUtils.getClassList(aPackage, Topic.class);
            for (Class<?> clazz : classList) {
                addClass(clazz);
            }
            System.out.println("add mapper:" + classList.size());
        }
    }

    public static void addClass(Class<?> clazz) {
        Topic annotation = clazz.getAnnotation(Topic.class);
        String value = annotation.value();
        mapper.put(value.substring(0, value.lastIndexOf("/")), clazz);
    }

    @Override
    public Message<?> toMessage(byte[] bytes, Map<String, Object> headers) {
        String topic = (String) headers.get(MqttHeaders.TOPIC);
        String action = topic.substring(0, topic.lastIndexOf("/"));

        if (encrypt.isEncrypt(topic)) {
            bytes = encrypt.doDecrypt(bytes);
        }

        Class clazz = mapper.get(action);
        Object payload;

        String str = new String(bytes, StandardCharsets.UTF_8);
        if (clazz != null) {
            try {
                payload = JSONObject.parseObject(str, clazz);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder("\n反序列化失败：");
                sb.append(clazz.getName());
                sb.append(topic);
                sb.append("\nstring ").append(str);
                log.error(sb.toString());
                throw e;
            }
        } else {
            payload = str;
        }

        MessageBuilder<Object> messageBuilder = MessageBuilder.withPayload(payload).copyHeaders(headers);

        return messageBuilder.build();
    }

    @Override
    public byte[] fromMessage(Message<?> message) {
        Object payload = message.getPayload();
        String str = JSONObject.toJSONString(payload);
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        return encrypt.isEncrypt(message) ? encrypt.doEncrypt(bytes) : bytes;
    }
}
