package com.stanwind.wmqtt;

import com.stanwind.wmqtt.auth.ClientApi;
import com.stanwind.wmqtt.auth.SettingFactory;
import com.stanwind.wmqtt.auth.aliyun.AliApi;
import com.stanwind.wmqtt.auth.aliyun.AliSignSettingFactory;
import com.stanwind.wmqtt.auth.beans.AuthBean;
import com.stanwind.wmqtt.auth.def.DefaultClientApi;
import com.stanwind.wmqtt.auth.def.DefaultSettingFactory;
import com.stanwind.wmqtt.message.BytesMessageConverter;
import com.stanwind.wmqtt.security.DefMessageEncrypt;
import com.stanwind.wmqtt.security.IMsgEncrypt;
import com.stanwind.wmqtt.security.TableMsgEncrypt;
import com.stanwind.wmqtt.utils.PlatformUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mapping.BytesMessageMapper;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

/**
 * MqttConfig
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-10 19:18
 **/
@Slf4j
@Configuration
@EnableIntegration
@IntegrationComponentScan(basePackages = "com.stanwind.wmqtt")
public class MqttConfig {

    public static final String CHANNEL_NAME_OUT = "mqttOutboundChannel";
    public static final String CHANNEL_NAME_IN = "mqttInboundChannel";
    public static final String DEVICE_ID_T = "deviceId";
    public static final String TASK_ID_T = "taskId";
    public static final String INSTANCE_ID_T = "instanceId";
    public static final String DEVICE_ID = "{" + DEVICE_ID_T + "}";

    //实例标志 MAC_PORT
    private static String L_INSTANCE_ID = "";

    public MqttPahoMessageDrivenChannelAdapter adapter;

    @Value("${mqtt.server-uris}")
    @Getter
    private String[] serverURIs;

    /**
     * 缺省外网地址则返回统一地址 否则返回该
     */
    @Value("${mqtt.pub-server-uris:}")
    @Getter
    private String[] pubServerURIs;

    /**
     * wss
     */
    @Value("${mqtt.websocket-uri:}")
    @Getter
    private String[] websocketUri;

    @Value("${mqtt.aliyun:false}")
    private Boolean aliyun;

    @Value("${mqtt.ali-instance}")
    private String aliInstance;

    @Value("${mqtt.access-key}")
    private String accessKey;

    @Value("${mqtt.secret-key}")
    private String secretKey;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.keep-alive-interval}")
    private int keepAliveInterval;

    @Value("${mqtt.sub-topics}")
    private String[] subTopics;

    @Value("${mqtt.message-mapper}")
    private Class<? extends BytesMessageMapper> mapperClass;

    @Value("${mqtt.client-id-prefix}")
    @Getter
    private String clientIdPrefix;

    @Value("${mqtt.acl.read}")
    @Getter
    private String aclRead;

    @Value("${mqtt.acl.write}")
    @Getter
    private String aclWrite;

    @Value("${mqtt.enc-table}")
    @Getter
    private String encTable;

    @Value("${mqtt.enc-count:0}")
    @Getter
    private Integer encCount;

    @Autowired
    private ServerProperties serverProperties;

    @Bean
    public AuthBean authBean() {
        return new AuthBean().setInstanceId(aliInstance)
                .setPassword(password).setUsername(username)
                .setAccessKey(accessKey).setSecretKey(secretKey);
    }

    @Bean
    public SettingFactory settingFactory() {
        //@ConditionOnBean
        if (aliyun) {
            return new AliSignSettingFactory();
        } else {
            return new DefaultSettingFactory();
        }
    }

    @Bean
    public ClientApi clientApi() {
        if (aliyun) {
            return new AliApi();
        } else {
            return new DefaultClientApi();
        }
    }

    /**
     * //固定为查表加密
     * @return
     */
    @Bean
    public IMsgEncrypt messageEncrypt() {
        if (encCount > 0 && !StringUtils.isEmpty(encTable)) {
            log.info("MQTT配置数据表加密");
            return new TableMsgEncrypt();
        } else {
            log.info("MQTT配置数据不加密");
            return new DefMessageEncrypt();
        }
    }

    @Bean
    public MqttMessageConverter bytesMessageConverter(@Value("${mqtt.model-packages}") String modelPackages
    , IMsgEncrypt encrypt)
            throws NoSuchMethodException {
        BytesMessageMapper bytesMessageMapper =
                BeanUtils.instantiateClass(mapperClass.getConstructor(String.class, IMsgEncrypt.class)
                        , modelPackages, encrypt);
        return new BytesMessageConverter(bytesMessageMapper);
    }

    public String getInstanceId() {
        if (StringUtils.isEmpty(L_INSTANCE_ID)) {
            L_INSTANCE_ID = PlatformUtils.getMACAddress() + "_" + serverProperties.getPort();
        }

        return L_INSTANCE_ID;
    }

    @Bean
    @ServiceActivator(inputChannel = CHANNEL_NAME_OUT)
    public MessageHandler mqttOutbound(MqttMessageConverter mqttMessageConverter, SettingFactory settingFactory)
            throws Exception {
        String clientId = clientIdPrefix + "_outbound_" + getInstanceId();

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = settingFactory
                .set(serverURIs, keepAliveInterval, authBean().setClientId(clientId));
        factory.setConnectionOptions(options);
        log.info("当前MQTT配置项: {}", options);

        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, factory);
        messageHandler.setConverter(mqttMessageConverter);
        messageHandler.setCompletionTimeout(5000);
        messageHandler.setAsync(true);
        log.info("outbound: {}", clientId);

        return messageHandler;
    }

    @Bean
    public MessageProducer mqttInbound(MqttMessageConverter mqttMessageConverter, SettingFactory settingFactory)
            throws Exception {
        String clientId = clientIdPrefix + "_inbound_" + getInstanceId();

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = settingFactory
                .set(serverURIs, keepAliveInterval, authBean().setClientId(clientId));
        factory.setConnectionOptions(options);

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId, factory,
                subTopics);
        adapter.setConverter(mqttMessageConverter);
        adapter.setOutputChannel(mqttInboundChannel());
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        this.adapter = adapter;
        log.info("inbound: {}", clientId);

        return adapter;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }


    public void addTopic(String... topic) {
        adapter.addTopic(topic);
    }

    public void addTopic(String topic, int qos) {
        adapter.addTopic(topic, qos);
    }

    public void addTopics(String[] topic, int[] qos) {
        adapter.addTopics(topic, qos);
    }

    public void removeTopic(String... topic) {
        adapter.removeTopic(topic);
    }
}
