package com.stanwind.wmqtt.beans;

/**
 * Constant
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2021-02-03 11:57 AM
 **/
public class Constant {
    public static final String CHANNEL_NAME_OUT = "mqttOutboundChannel";
    public static final String CHANNEL_NAME_IN = "mqttInboundChannel";
    public static final String DEVICE_ID_T = "deviceId";
    public static final String TASK_ID_T = "taskId";
    public static final String INSTANCE_ID_T = "instanceId";
    public static final String DEVICE_ID = "{" + DEVICE_ID_T + "}";
    public static final String BASE_MSG_PACKAGE = "com.stanwind.wmqtt.message";
}
