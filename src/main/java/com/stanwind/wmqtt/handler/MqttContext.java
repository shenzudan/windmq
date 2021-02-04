package com.stanwind.wmqtt.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * MqttContext 上下文 存储路径参数相关
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-12 17:24
 **/
public class MqttContext {
    private static final ThreadLocal<MqttContext> appContext = new ThreadLocal<>();
    private final HashMap<String, Object> values = new HashMap<String, Object>();

    public static final String KEY_PARAMS = "CACHE_PARAM_KEY";
    public static MqttContext getContext() {
        MqttContext context = appContext.get();
        if (context == null) {
            context = new MqttContext();
            appContext.set(context);
        }

        return context;
    }

    public void clear() {
        MqttContext context = appContext.get();
        if (context != null) {
            context.values.clear();
        }

        context = null;
    }

    public void addObject(String key, Object value) {
        values.put(key, value);
    }

    public Object getObject(String key) {
        return values.get(key);
    }

    public void removeObject(String key) {
        values.remove(key);
    }

    public Map<String, String> getParams() {
        return (Map<String, String>) getObject(KEY_PARAMS);
    }

    public void putParams(Map<String, String> map) {
        addObject(KEY_PARAMS, map);
    }
}
