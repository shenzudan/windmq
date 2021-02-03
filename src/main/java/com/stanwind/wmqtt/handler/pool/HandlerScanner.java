package com.stanwind.wmqtt.handler.pool;

import com.stanwind.wmqtt.anno.TopicHandler;
import com.stanwind.wmqtt.handler.TopicPattern;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * HandlerScanner 处理器注解扫描器
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 18:45
 **/
@Component
public class HandlerScanner implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(HandlerScanner.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("start to scan topic handlers");
        //遍历所有bean
        String[] beans = applicationContext.getBeanDefinitionNames();
        for (String beanName : beans) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanType = AopUtils.getTargetClass(bean);
            if (Objects.isNull(beanType)) {
                continue;
            }

            for (Method method : beanType.getMethods()) {
                for (Annotation anno : method.getDeclaredAnnotations()) {
                    if (anno.annotationType().equals(TopicHandler.class)) {
                        MsgHandlerDefinition def = createHandler((TopicHandler) anno, beanType, method);
                        MsgAdapter adapter = MsgAdapter.valueOf(def);
                        if (def.isFull()) {
                            MsgAdapter.registFullTopic(def.getTopic(), adapter);
                        } else if (regFuzzy((TopicHandler) anno)) {
                            MsgAdapter.registRegTopic(def.getPatternDefinition().getRegTxt(), adapter);
                        } else {
                            MsgAdapter.registFuzzyTopic(def.getTopic(), adapter);
                        }

                        MQTTHandleTask.map.put(beanType, bean);
                    }
                }
            }
        }
        log.info("finish scan topic handlers");
    }

    private boolean regFuzzy(TopicHandler anno) {
        return !anno.full() && anno.topic().contains("{") && anno.topic().contains("}");
    }

    private MsgHandlerDefinition createHandler(TopicHandler anno, Class<?> beanType, Method method) {
        MsgHandlerDefinition definition = new MsgHandlerDefinition().setClaz(beanType).setFull(anno.full())
                .setMethod(method)
                .setTopic(anno.topic());
        String topic = anno.topic();
        //处理完整
        if (regFuzzy(anno)) {
            definition.setPatternDefinition(TopicPattern.prepare(topic));
        }

        return definition;
    }
}
