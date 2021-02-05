# windmq - MQTT快速开发脚手架
<a href="https://gitee.com/sense7/windmq/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-Apache%202-blue" /></a>
<a href='https://gitee.com/sense7/windmq/stargazers'><img src='https://gitee.com/sense7/windmq/badge/star.svg?theme=dark' alt='star'></img></a>
<a href='https://gitee.com/sense7/windmq/members'><img src='https://gitee.com/sense7/windmq/badge/fork.svg?theme=dark' alt='fork'></img></a>

### 前言
快速开发处理MQTT topic，一个方法注解就搞定

原样从项目里搬出来的，产线阿里云，测试EMQ，需要统一支持下

有些config和bean不太合理，过年有空整理下

此项目整合springboot部分和topic规则搬运了一个项目，刚接触这个，十分感谢前辈的经验https://gitee.com/yezhihao/mqtt-sample

关于共享订阅的高可用兼容，如果有方案还望各位不吝赐教

### 功能
- MQTT客户端登录凭证分配(ACL支持阿里云\EMQ目前只支持账号密码，可自定义实现)
- 适合低端设备的查表加密协议(详情见: com.stanwind.wmqtt.security.TableMsgEncrypt)
- 高可用部署(多实例不同clientID上线，EMQ有提供共享订阅，但是阿里云只能靠规则引擎转发MQ，我们线上使用全盘负责机制，谁发命令谁处理)
- 消息处理池(CPU核心数*2 + 1, )
- Topic注解匹配消息处理,支持模糊匹配(正则实现，可取topic路径参数)和精确匹配

### 默认规则
- 对客户端发送的TOPIC均以 IOT_CLIENT/xxx形式 (配置可修改)
- 对服务端发送的TOPIC均以 IOT_SERVER/xxx形式 (配置可修改)
- 加密行需在payload开头2 byte表示采用哪一行数据进行加密 (若启用加密则IOT开头的topic均会加密，详见：com.stanwind.wmqtt.security.IotDeviceMessageEncrypt)
- 为兼容阿里云 clientId均以GID_DEVICE@@@开头 (配置可修改)
- Server采用签名登录，阿里云环境下Client分配的账号密码使用token登录，鉴权信息有效时长12小时
- topic中{instanceId}表示匹配当前实例ID，{deviceId}表示匹配当前设备序列号(详情: com.stanwind.wmqtt.MqttConfig)

### 默认spring-boot依赖
```maven
 <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.0.7.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
  </parent>
```

### 项目仓库
```xml
    <dependency>
  <groupId>com.stanwind</groupId>
  <artifactId>spring-boot-windmq</artifactId>
  <version>1.0.0-RELEASE</version>
</dependency>
```

### 样例工程
https://gitee.com/sense7/windmq-demo.git

### 参考依赖
```xml
    <!-- windmq dependency -->
<dependency>
  <groupId>com.stanwind</groupId>
  <artifactId>spring-boot-windmq</artifactId>
  <version>1.0.0-RELEASE</version>
</dependency>

  <!-- MQTT -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.integration</groupId>
<artifactId>spring-integration-mqtt</artifactId>
<exclusions>
  <exclusion>
    <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
    <groupId>org.eclipse.paho</groupId>
  </exclusion>
</exclusions>
</dependency>
  <!-- 1.2.0 版本有bug -->
<dependency>
<groupId>org.eclipse.paho</groupId>
<artifactId>org.eclipse.paho.client.mqttv3</artifactId>
<version>1.2.1</version>
</dependency>
```

### 启用windmq
```java
@EnableWindMQ
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### 使用样例
- 临时订阅/取消(注入ProducerHolder)
```java
void addTopic(String... topic);
        void addTopic(String topic, int qos);
        void addTopics(String[] topic, int[] qos);
        void removeTopic(String... topic);
```

- 消息发送 IMessageService
```java
void notify(String deviceId, Object payload);
        void sendToTopic(String topic, Object payload);
        MqttResponse request(String deviceId, MqttRequest payload);
        MqttResponse request(String deviceId, MqttRequest payload, long timeout);
```

- 消息处理
```java
@TopicHandler(topic = "$SYS/brokers/{node}/clients/{deviceId}/connected")
public void connected(MQTTMsg msg) {
        ClientReqVO clientReqVO = JSONObject.parseObject(msg.getPayload().toString(), ClientReqVO.class);
        process(clientReqVO);
        }
```

- 获取路径参数
```java
@Service
public class DemoHandler extends BaseTopicHandler {
    @TopicHandler(topic = "IOT_SERVER/ping/{instanceId}/{taskId}/{param1}")
    public void uploadPingData(MQTTMsg msg) {
        String taskId = getParam("taskId");
        String param1 = getParam("param1");
        //或
        MqttContext.getContext().getParams().getOrDefault("taskId", null);
    }
}
```

- 高可用方案(临时订阅处理完取消 适用于服务端发送控制指令，携带临时随机topic，客户端往服务端指定topic写)
```java
@TopicHandler(topic = "IOT_SERVER/ping/{instanceId}/{taskId}")
public void uploadPingData(MQTTMsg msg) {
        if (!currentHandle()) {
        log.debug("非当前实例任务: [{}]", msg);
        return;
        }

        if (接收完毕) {
        //取消订阅
        mqttConfig.removeTopic(msg.getTopic());
        }
        }
```

- 生成客户端链接数据
```java
@Autowired
private MqttConfig mqttConfig;
@Autowired
private ClientApi clientApi;
public MqttConnVO generateMqttConnConfig(String sn) throws Exception {
        String r = mqttConfig.getAclRead().replaceAll(DEVICE_ID, stcUtil.sn2cli(sn));
        String w = mqttConfig.getAclWrite().replaceAll(DEVICE_ID, stcUtil.sn2cli(sn));
        ConnData connData = clientApi.getTokenConn(Utils.splitToList(r), Utils.splitToList(w));
        MqttConnVO vo = new MqttConnVO();
        //缺省外网地址则返回统一地址 否则返回外网地址
        vo.setUris(ArrayUtils.isEmpty(mqttConfig.getPubServerURIs()) ? mqttConfig.getServerURIs() : mqttConfig.getPubServerURIs());
        vo.setReadTopics(r);
        vo.setWriteTopics(w);
        vo.setEnc(mqttConfig.getEncTable());
        vo.setEncSize(mqttConfig.getEncCount());
        BeanUtils.copyProperties(connData, vo);
        log.info("{} 获取mqtt: {}", sn, vo);

        return vo;
        }

@Data
public class MqttConnVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String[] uris;
    private Long expire;
    private String username;
    private String password;
    private String readTopics;
    private String writeTopics;
    private String enc;
    private Integer encSize;
}
```

### 使用建议
- 配置参见bootstrap.yml
- 测试环境使用EMQ，产线使用阿里云
