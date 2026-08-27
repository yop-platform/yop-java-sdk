package com.yeepay.yop.sdk.utils.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

    /**
     * 原始报文透传属性，由调用方设置，见{@code JsonUtils#load(String, Object)}
     */
    public static final String ATTR_RAW_JSON = "yop.sdk.raw.json";

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * StringReader#str，JDK16+默认不开放java.io、JDK24+已无该字段，反射失败后不再重试
     */
    private static volatile Field STRING_READER_STR_FIELD;
    private static volatile boolean STRING_READER_REFLECTION_UNAVAILABLE;

    @Override
    public String deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        if (jp.isExpectedStartObjectToken() || jp.isExpectedStartArrayToken()) {
            final String rawJson = resolveRawJson(jp, context);
            // 拿不到原始报文时，用解析器重建等价报文，不可返回null
            if (null == rawJson) {
                return rebuildJson(jp);
            }
            final int startLocation = (int) jp.getCurrentLocation().getCharOffset();
            jp.skipChildren();
            final int endLocation = (int) jp.getCurrentLocation().getCharOffset();
            if (startLocation < 1 || startLocation > endLocation || endLocation > rawJson.length()) {
                throw new JsonParseException(jp, "raw json location illegal, start:" + startLocation
                        + ", end:" + endLocation + ", rawJsonLength:" + rawJson.length());
            }
            return rawJson.substring(startLocation - 1, endLocation);
        } else if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            return "\"" + jp.getText() + "\"";
        } else {
            return jp.getText();
        }
    }

    /**
     * 获取原始报文，取不到时返回null
     *
     * @param jp      解析器
     * @param context 上下文
     * @return 原始报文
     */
    private String resolveRawJson(JsonParser jp, DeserializationContext context) {
        // 调用方透传的原始报文，与JDK、jackson版本无关，优先使用
        final Object rawJsonAttr = context.getAttribute(ATTR_RAW_JSON);
        if (rawJsonAttr instanceof String) {
            return (String) rawJsonAttr;
        }

        // 未透传时(如外部直接使用ObjectMapper)，退化为从解析器的源对象获取
        final Object sourceRef = jp.getCurrentLocation().getSourceRef();
        if (sourceRef instanceof String) {
            return (String) sourceRef;
        }
        // json长度超过0x8000时，jackson会包装为StringReader，仅JDK15及以下可反射获取
        if (sourceRef instanceof StringReader) {
            return getStringViaReflection((StringReader) sourceRef);
        }
        return null;
    }

    /**
     * StringReader -> String
     * 没有通过StringReader的接口来获取String，是因为读取StringReader会改变对象本身的状态，可能会造成其它影响
     *
     * @return 原始报文，不可用时返回null
     */
    private String getStringViaReflection(StringReader reader) {
        if (STRING_READER_REFLECTION_UNAVAILABLE) {
            return null;
        }
        try {
            Field strField = STRING_READER_STR_FIELD;
            if (null == strField) {
                strField = StringReader.class.getDeclaredField("str");
                strField.setAccessible(true);
                STRING_READER_STR_FIELD = strField;
            }
            return (String) strField.get(reader);
        } catch (Throwable t) {
            // 高版本JDK不支持，标记后不再重试，避免每次调用都构造异常
            STRING_READER_REFLECTION_UNAVAILABLE = true;
            LOGGER.warn("StringReader#str inaccessible, raw json will be rebuilt, ex:{}", t.toString());
            return null;
        }
    }

    /**
     * 用解析器重建等价的json文本，数字保留原始字面量，避免精度、末尾零丢失
     *
     * @param jp 解析器，当前token为START_OBJECT或START_ARRAY
     * @return 重建后的json文本
     */
    private String rebuildJson(JsonParser jp) throws IOException {
        final StringWriter writer = new StringWriter();
        final JsonGenerator generator = JSON_FACTORY.createGenerator(writer);
        try {
            int depth = 0;
            while (true) {
                final JsonToken token = jp.getCurrentToken();
                if (JsonToken.START_OBJECT == token || JsonToken.START_ARRAY == token) {
                    depth++;
                } else if (JsonToken.END_OBJECT == token || JsonToken.END_ARRAY == token) {
                    depth--;
                }
                if (JsonToken.VALUE_NUMBER_INT == token || JsonToken.VALUE_NUMBER_FLOAT == token) {
                    generator.writeNumber(jp.getText());
                } else {
                    generator.copyCurrentEvent(jp);
                }
                if (depth <= 0) {
                    break;
                }
                jp.nextToken();
            }
        } finally {
            generator.close();
        }
        return writer.toString();
    }
}
