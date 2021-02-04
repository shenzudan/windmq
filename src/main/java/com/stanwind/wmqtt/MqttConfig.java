package com.stanwind.wmqtt;

import static com.stanwind.wmqtt.beans.Constant.CHANNEL_NAME_OUT;

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
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@Configuration
@EnableIntegration
@IntegrationComponentScan(basePackages = "com.stanwind.wmqtt")
public class MqttConfig {

    private static final Logger log = LoggerFactory.getLogger(MqttConfig.class);

    /**
     * 实例标志 MAC_PORT
     */
    private static String L_INSTANCE_ID = "";

    @Value("${mqtt.server-uris}")
    private String[] serverURIs;

    /**
     * 缺省外网地址则返回统一地址 否则返回该
     */
    @Value("${mqtt.pub-server-uris:}")
    private String[] pubServerURIs;

    /**
     * wss
     */
    @Value("${mqtt.websocket-uri:}")
    private String[] websocketUri;

    @Value("${mqtt.aliyun:false}")
    private Boolean aliyun;

    @Value("${mqtt.ali-instance:}")
    private String aliInstance;

    @Value("${mqtt.access-key:}")
    private String accessKey;

    @Value("${mqtt.secret-key:}")
    private String secretKey;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.keep-alive-interval:}")
    private int keepAliveInterval;

    @Value("${mqtt.sub-topics:}")
    private String[] subTopics;

    @Value("${mqtt.message-mapper:com.stanwind.wmqtt.message.MqttJsonMessageMapper}")
    private Class<? extends BytesMessageMapper> mapperClass;

    @Value("${mqtt.client-id-prefix:}")
    private String clientIdPrefix;

    @Value("${mqtt.acl.read:}")
    private String aclRead;

    @Value("${mqtt.acl.write:}")
    private String aclWrite;

    @Value("${mqtt.enc-table:}")
    private String encTable;

    @Value("${mqtt.enc-count:0}")
    private Integer encCount;

    public String[] getServerURIs() {
        return serverURIs;
    }

    public String[] getPubServerURIs() {
        return pubServerURIs;
    }

    public String[] getWebsocketUri() {
        return websocketUri;
    }

    public Boolean getAliyun() {
        return aliyun;
    }

    public String getAliInstance() {
        return aliInstance;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public String[] getSubTopics() {
        return subTopics;
    }

    public String getClientIdPrefix() {
        return clientIdPrefix;
    }

    public String getAclRead() {
        return aclRead;
    }

    public String getAclWrite() {
        return aclWrite;
    }

    public String getEncTable() {
        return encTable;
    }

    public Integer getEncCount() {
        return encCount;
    }

    /**
     * 配置鉴权基础数据包装bean
     * @return
     */
    @Bean
    public AuthBean authBean() {
        return new AuthBean().setInstanceId(aliInstance)
                .setPassword(password).setUsername(username)
                .setAccessKey(accessKey).setSecretKey(secretKey);
    }

    /**
     * 连接数据生成工厂
     * @return
     */
    @Bean
    public SettingFactory settingFactory() {
        //@ConditionOnBean
        if (aliyun) {
            return new AliSignSettingFactory();
        } else {
            return new DefaultSettingFactory();
        }
    }

    /**
     * mqtt实例接口请求工具
     * @return
     */
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

    /**
     * message包装加密配置
     * @param modelPackages
     * @param encrypt
     * @return
     * @throws NoSuchMethodException
     */
    @Bean
    public MqttMessageConverter bytesMessageConverter(@Value("${mqtt.model-packages:com.stanwind.wmqtt.message}") String modelPackages
            , IMsgEncrypt encrypt)
            throws NoSuchMethodException {
        BytesMessageMapper bytesMessageMapper =
                BeanUtils.instantiateClass(mapperClass.getConstructor(String.class, IMsgEncrypt.class)
                        , modelPackages, encrypt);
        return new BytesMessageConverter(bytesMessageMapper);
    }

    /**
     * 实例名规则
     */
    public String getInstanceId() {
        if (StringUtils.isEmpty(L_INSTANCE_ID)) {
            L_INSTANCE_ID = PlatformUtils.getMACAddress() + "_" + PlatformUtils.JVMPid();
        }

        return L_INSTANCE_ID;
    }

    /**
     * mqtt发送bean
     * @param mqttMessageConverter
     * @param settingFactory
     * @return
     * @throws Exception
     */
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

    /**
     * mqtt接收bean
     * @param mqttMessageConverter
     * @param settingFactory
     * @return
     * @throws Exception
     */
    @Bean
    @Primary
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

    /**
     * 发送客户端holder
     * @param producer
     * @return
     */
    @Bean
    public ProducerHolder getWindmq(MessageProducer producer) {
        return new ProducerHolder((MqttPahoMessageDrivenChannelAdapter) producer);
    }
}
