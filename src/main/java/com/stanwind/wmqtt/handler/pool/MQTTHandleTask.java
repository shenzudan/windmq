package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.MqttContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * MQTTHandleTask 处理任务
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:51
 **/

public class MQTTHandleTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MQTTHandleTask.class);

    /**
     * 把注册过处理器的spring bean都存到这
     */
    public static final Map<Class<?>, Object> map = new ConcurrentHashMap<>();
    private MsgHandlerDefinition definition;
    private MQTTMsg msg;

    public MQTTHandleTask(MsgHandlerDefinition definition, MQTTMsg msg) {
        this.definition = definition;
        this.msg = msg;
    }

    /**
     * 反射调用包装方法
     */
    @Override
    public void run() {
        Object bean = map.get(definition.getClaz());
        MqttContext.getContext().putParams(msg.getValues());
        try {
            ReflectionUtils.invokeMethod(definition.getMethod(), bean, msg);
        } catch (Throwable e) {
            log.error("处理MQTT消息出现错误", e);
        } finally {
            MqttContext.getContext().clear();
        }
    }
}
