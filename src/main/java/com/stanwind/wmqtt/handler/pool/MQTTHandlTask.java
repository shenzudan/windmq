package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.MqttContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

/**
 * MQTTHandlerTask 处理任务
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:51
 **/
@AllArgsConstructor
@Slf4j
public class MQTTHandlTask implements Runnable {
    /**把注册过处理器的spring bean都存到这 */
    public static final Map<Class<?>, Object> map = new ConcurrentHashMap<>();
    private MsgHandlerDefinition definition;
    private MQTTMsg msg;

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
