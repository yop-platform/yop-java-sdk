package com.yeepay.yop.sdk.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;

/**
 * title: 保留Json对象为raw string的形式，不尝试解析它<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 17/10/19 下午8:36
 */
public class KeepAsRawStringDeserializer extends JsonDeserializer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeepAsRawStringDeserializer.class);

    @Override
    public String deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        if (jp.isExpectedStartObjectToken() || jp.isExpectedStartArrayToken()) {
            String rawJson = getStringFromSource(jp.getCurrentLocation().getSourceRef());
            int startLocation = (int) jp.getCurrentLocation().getCharOffset();
            jp.skipChildren();
            int endLocation = (int) jp.getCurrentLocation().getCharOffset();
            return rawJson.substring(startLocation - 1, endLocation);
        } else if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            return "\"" + jp.getText() + "\"";
        } else {
            return jp.getText();
        }
    }

    /**
     * sourceRef可能是String或者StringReader类型，为什么不定义统一的接口呢......
     *
     * @param sourceRef
     * @return
     */
    private String getStringFromSource(Object sourceRef) {
        if (sourceRef instanceof String) {
            return (String) sourceRef;
        }
        if (sourceRef instanceof StringReader) {
            return getStringViaReflection((StringReader) sourceRef);
        }
        throw new IllegalArgumentException("source ref of json is not an instance of String/StringReader,can't use KeepAsRawStringDeserializer here!");
    }

    /**
     * StringReader -> String
     * 没有通过StringReader的接口来获取String，是因为读取StringReader会改变对象本身的状态，可能会造成其它影响
     *
     * @return
     */
    private String getStringViaReflection(StringReader reader) {
        try {
            Field strField = StringReader.class.getDeclaredField("str");
            strField.setAccessible(true);
            return (String) strField.get(reader);
        } catch (Exception e) {
            LOGGER.error("error when get str from StringReader via reflection", e);
            return null;
        }
    }
}
