package com.stanwind.wmqtt.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TopicPattern 正则topic匹配
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-12 12:59
 **/
public class TopicPattern {

    final static String ptn = "(.*?)";
    final static String pt = "\\{" + ptn + "\\}";
    final static Pattern pp = Pattern.compile(pt);

    @Data
    @Accessors(chain = true)
    public static class TopicPatternDefinition {

        String regTxt;
        Pattern pattern;
        Map<String, Integer> id2pos;
    }

    public static TopicPatternDefinition prepare(String topic) {
        Map<String, Integer> id2pos = new HashMap<>();

        Matcher m = pp.matcher(topic);
        int i = 0;
        String rTopic = topic;
        while (m.find()) {
            String t = m.group();
            id2pos.put(t.replace("{", "").replace("}", ""), i++);
            rTopic = rTopic.replace(t, ptn);
        }

        //如果是$开头的系统级别
        if (rTopic.startsWith("$")) {
            rTopic = rTopic.replace("$", "\\$");
        }

        rTopic += "$";

        return new TopicPatternDefinition().setId2pos(id2pos).setPattern(Pattern.compile(rTopic)).setRegTxt(rTopic);
    }

    public static boolean match(String topic, TopicPatternDefinition topicPatternDefinition) {
        return topicPatternDefinition.getPattern().matcher(topic).matches();
    }

    public static Map<String, String> getValueMap(String topic, TopicPatternDefinition topicPatternDefinition) {
        Matcher m = topicPatternDefinition.getPattern().matcher(topic);
        Map<String, String> values = new HashMap<>();
        if (!m.find()) {
            return values;
        }

        topicPatternDefinition.getId2pos().forEach((key, value) -> values.put(key, m.group(value + 1)));

        return values;
    }

    public static void main(String[] args) {
        String txt = "$system/{a1b}/{cs2d}/{ef2g}/snnnn/{aaaaa}";
        TopicPatternDefinition d = prepare(txt);

        System.out.println("id2pos ---- " + d);

        String topic = "$system/abvalue/cdvalue/evalue/snnnn/avalue";
        System.out.println("match --- " + match(topic, d));
        System.out.println(getValueMap(topic, d));

        topic = "$system/ab2value/cd2value/eva2lue/snnnn/ava2lue";
        System.out.println("match --- " + match(topic, d));
        System.out.println(getValueMap(topic, d));
    }
}
