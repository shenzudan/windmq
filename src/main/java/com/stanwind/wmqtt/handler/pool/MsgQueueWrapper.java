package com.stanwind.wmqtt.handler.pool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MsgSync
 * topic时许保证同步器
 * @author : Stan
 * @version : 1.0
 * @date :  2021-02-19 11:43 AM
 **/
public class MsgQueueWrapper {

    private volatile Map<String /* 精确具体的topic */, LinkedBlockingQueue<MQTTMsg>> msgList = new HashMap<>();

    /**
     * 往topic处理消息队列中投递数据
     * 如果当前queue新增 则返回队列 启动线程去处理
     *
     * @param msg
     * @return null 则不需要处理
     * @throws InterruptedException
     */
    public LinkedBlockingQueue<MQTTMsg> getMsgQueue(MQTTMsg msg) throws InterruptedException {
        LinkedBlockingQueue<MQTTMsg> msgQueue = null;
        //同topic消息时序保证
        String topic = msg.getTopic();
        if (!msgList.containsKey(topic)) {
            synchronized (this) {
                if (!msgList.containsKey(topic)) {
                    //理论上调用该方法应该是个单线程 dispatcher  不需要加锁
                    msgQueue = new LinkedBlockingQueue<>();
                    msgList.put(topic, msgQueue);
                }
            }
        }

        LinkedBlockingQueue<MQTTMsg> mqttMsgs = msgList.get(topic);
        //ordered
        mqttMsgs.put(msg);

        return msgQueue;
    }

    /**
     * 指定topic线程结束，移出key队列
     * @param topic
     */
    public boolean removeKey(String topic) {
        if (!msgList.containsKey(topic)) {
            return true;
        }

        synchronized (this) {
            if (msgList.containsKey(topic)) {
                LinkedBlockingQueue<MQTTMsg> mqttMsgs = msgList.get(topic);
                if (mqttMsgs.size() > 0) {
                    return false;
                }

                msgList.remove(topic);
            }
        }

        return true;
    }
}
