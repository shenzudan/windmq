package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.MqttContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
    private LinkedBlockingQueue<MQTTMsg> queue;
    private MsgQueueWrapper wrapper;
    /* 线程任务启动的 topic */
    private String topic;

    public MQTTHandleTask(MsgHandlerDefinition definition, LinkedBlockingQueue<MQTTMsg> queue, MsgQueueWrapper wrapper, String topic) {
        this.definition = definition;
        this.queue = queue;
        this.wrapper = wrapper;
        this.topic = topic;
    }

    /* 处理的任务计数 */
    private int count;

    /**
     * 处理完毕后remove key
     */
    @Override
    public void run() {
        Object bean = map.get(definition.getClaz());
        boolean loop;

        do {
            loop = false;
            try {
                while (true) {
                    MQTTMsg msg = queue.poll(5, TimeUnit.SECONDS);
                    if (msg == null) {
                        break;
                    }

                    process1(bean, msg);
                    count++;
                }
            } catch (InterruptedException e) {
                log.error("msg queue poll error", e);
            } finally {
                //移除过程中失败 说明当前queue还有任务进入
                loop = !wrapper.removeKey(topic);
            }
        } while (loop);

        log.info("当前线程{}单次处理mqtt消息{}条", topic, count);
    }

    private void process1(Object bean, MQTTMsg msg) {
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
