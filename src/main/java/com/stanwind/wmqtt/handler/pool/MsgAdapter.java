package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.handler.TopicPattern;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MsgAdapter 协议分拨器 目前先String消息 直接解析TOPIC
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:49
 **/
@AllArgsConstructor
public class MsgAdapter {
    private static Map<String /* TOPIC */, MsgAdapter> FULL_TOPIC = new ConcurrentHashMap();
    private static Map<String /* TOPIC */, MsgAdapter> FUZZY_TOPIC = new ConcurrentHashMap();
    private static Map<String /* TOPIC */, MsgAdapter> REG_TOPIC = new ConcurrentHashMap();

    @Getter
    private MsgHandlerDefinition handler = null;

    public static MsgAdapter valueOf(MsgHandlerDefinition definition) {
        return new MsgAdapter(definition);
    }

    public static MsgAdapter registFullTopic(String topic, MsgAdapter adapter) {
        return (MsgAdapter) FULL_TOPIC.put(topic, adapter);
    }

    public static MsgAdapter registFuzzyTopic(String topic, MsgAdapter adapter) {
        return (MsgAdapter) FUZZY_TOPIC.put(topic, adapter);
    }

    public static MsgAdapter registRegTopic(String topic, MsgAdapter adapter) {
        return (MsgAdapter) REG_TOPIC.put(topic, adapter);
    }

    public static boolean isContainsFullTopic(String topic) {
        return FULL_TOPIC.containsKey(topic);
    }

    public static boolean isContainsFuzzyTopic(String topic) {
        return FUZZY_TOPIC.containsKey(topic);
    }

    public static MsgAdapter transRegAdapter(String topic) {
        return REG_TOPIC.values().parallelStream().filter(msgAdapter ->
                TopicPattern.match(topic, msgAdapter.handler.getPatternDefinition())
        ).findFirst().orElse(null);
    }

    public static MsgAdapter unregistFullTopic(String topic) {
        return (MsgAdapter) FULL_TOPIC.remove(topic);
    }

    public static MsgAdapter unregistFuzzyTopic(String topic) {
        return (MsgAdapter) FUZZY_TOPIC.remove(topic);
    }


    public static MsgAdapter getFullTopicAdapter(String topic) {
        return (MsgAdapter) FULL_TOPIC.get(topic);
    }

    public static MsgAdapter getFuzzyTopicAdapter(String topic) {
        return (MsgAdapter) FUZZY_TOPIC.get(topic);
    }

    public void processMsg(MQTTMsg msg) {
        MsgHandlerPool.Instance.getInstance().submitTask(new MQTTHandlerTask(this.handler, msg));
    }
}
