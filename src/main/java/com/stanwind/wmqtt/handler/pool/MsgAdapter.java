package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.TopicPattern;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MsgAdapter 协议分拨器 目前先String消息 直接解析TOPIC
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:49
 **/
public class MsgAdapter {

    private static Map<String /* TOPIC */, MsgAdapter> FULL_TOPIC = new ConcurrentHashMap();
    private static Map<String /* TOPIC */, MsgAdapter> FUZZY_TOPIC = new ConcurrentHashMap();
    private static Map<String /* TOPIC */, MsgAdapter> REG_TOPIC = new ConcurrentHashMap();

    public MsgAdapter(MsgHandlerDefinition handler) {
        this.handler = handler;
    }

    private final MsgHandlerDefinition handler;

    /**
     * 获取处理器定义
     * @return 当前处理器
     */
    public MsgHandlerDefinition getHandler() {
        return handler;
    }

    /**
     * 静态工厂方法生成adapter
     * @param definition
     * @return
     */
    public static MsgAdapter valueOf(MsgHandlerDefinition definition) {
        return new MsgAdapter(definition);
    }

    /**
     * 注册全路径匹配adapter
     * @param topic
     * @param adapter
     * @return adapter
     */
    public static MsgAdapter registFullTopic(String topic, MsgAdapter adapter) {
        return (MsgAdapter) FULL_TOPIC.put(topic, adapter);
    }

    /**
     * 注册模糊路径匹配adapter
     * @param topic
     * @param adapter
     * @return adapter
     */
    public static MsgAdapter registFuzzyTopic(String topic, MsgAdapter adapter) {
        return (MsgAdapter) FUZZY_TOPIC.put(topic, adapter);
    }

    /**
     * 注册正则路径匹配adapter
     * @param topic
     * @param adapter
     * @return adapter
     */
    public static MsgAdapter registRegTopic(String topic, MsgAdapter adapter) {
        return (MsgAdapter) REG_TOPIC.put(topic, adapter);
    }

    /**
     * 判断topic是否在全路径中注册
     * @param topic
     * @return
     */
    public static boolean isContainsFullTopic(String topic) {
        return FULL_TOPIC.containsKey(topic);
    }

    /**
     * 判断topic是否在模糊路径中注册
     * @param topic
     * @return
     */
    public static boolean isContainsFuzzyTopic(String topic) {
        return FUZZY_TOPIC.containsKey(topic);
    }

    /**
     * 判断topic是否在正则匹配路径中注册
     * @param topic
     * @return
     */
    public static MsgAdapter transRegAdapter(String topic) {
        return REG_TOPIC.values().parallelStream().filter(msgAdapter ->
                TopicPattern.match(topic, msgAdapter.handler.getPatternDefinition())
        ).findFirst().orElse(null);
    }

    /**
     * 移除全路径注册
     * @param topic
     * @return
     */
    public static MsgAdapter unregistFullTopic(String topic) {
        return (MsgAdapter) FULL_TOPIC.remove(topic);
    }

    /**
     * 移除模糊路径注册
     * @param topic
     * @return
     */
    public static MsgAdapter unregistFuzzyTopic(String topic) {
        return (MsgAdapter) FUZZY_TOPIC.remove(topic);
    }

    /**
     * 获取全路径adapter
     * @param topic
     * @return
     */
    public static MsgAdapter getFullTopicAdapter(String topic) {
        return (MsgAdapter) FULL_TOPIC.get(topic);
    }

    /**
     * 获取模糊路径adapter
     * @param topic
     * @return
     */
    public static MsgAdapter getFuzzyTopicAdapter(String topic) {
        return (MsgAdapter) FUZZY_TOPIC.get(topic);
    }

    /**
     * 消息扔入线程池处理当前消息
     * @param msg
     */
    public void processMsg(MQTTMsg msg) {
        MsgHandlerPool.Instance.getInstance().submitTask(new MQTTHandleTask(this.handler, msg));
    }
}
