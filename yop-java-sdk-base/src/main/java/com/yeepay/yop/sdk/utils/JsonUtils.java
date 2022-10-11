package com.yeepay.yop.sdk.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.json.joda.DatetimeModule;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static com.yeepay.yop.sdk.YopConstants.*;

public class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        OBJECT_MAPPER.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        OBJECT_MAPPER.registerModule(new JodaModule());
        OBJECT_MAPPER.registerModule(new DatetimeModule());
        // 小数都用BigDecimal，默认的是Double
        OBJECT_MAPPER.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        OBJECT_MAPPER.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));

        // 指定jsonpath provider
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    private static final ObjectWriter writer = OBJECT_MAPPER.writer();
    private static final ObjectWriter prettyWriter = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    public static String toJsonPrettyString(Object value) throws JsonProcessingException {
        return prettyWriter.writeValueAsString(value);
    }

    public static String toJsonString(Object value) {
        try {
            return writer.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the deserialized object from the given json string and target class; or null if the given json string is
     * null.
     */
    public static <T> T fromJsonString(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new YopClientException("Unable to parse Json String.", e);
        }
    }

    public static JsonNode jsonNodeOf(String json) {
        return fromJsonString(json, JsonNode.class);
    }

    public static JsonGenerator jsonGeneratorOf(Writer writer) throws IOException {
        return new JsonFactory().createGenerator(writer);
    }

    public static <T> T loadFrom(File file, Class<T> clazz) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(file, clazz);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void load(InputStream input, Object obj) throws IOException {
        OBJECT_MAPPER.readerForUpdating(obj).readValue(input);
    }

    public static void load(String content, Object obj) throws IOException {
        OBJECT_MAPPER.readerForUpdating(obj).readValue(content);
    }

    public static <T> T loadFrom(InputStream input, Class<T> clazz)
            throws IOException {
        return OBJECT_MAPPER.readValue(input, clazz);
    }

    public static <T> T loadFrom(String content, Class<T> clazz)
            throws IOException {
        return OBJECT_MAPPER.readValue(content, clazz);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static ObjectWriter getWriter() {
        return writer;
    }

    public static ObjectWriter getPrettywriter() {
        return prettyWriter;
    }

    public static boolean isTotalEncrypt(Set<String> jsonPaths) {
        boolean totalEncrypt = CollectionUtils.isSubCollection(jsonPaths, JSON_PATH_ROOT);
        if (totalEncrypt) {
            return true;
        }
        if (jsonPaths.size() > 1 && !CollectionUtils.intersection(jsonPaths, JSON_PATH_ROOT).isEmpty()) {
            throw new YopClientException("illegal json paths:" + jsonPaths);
        }
        return false;
    }

    /**
     * 正序排列，保证优先加密对象
     *
     * @param jsonContent
     * @param jsonPathPatterns
     * @return
     */
    public static Set<String> resolveAllJsonPaths(String jsonContent, Set<String> jsonPathPatterns) {
        DocumentContext pathReadCtx = JsonPath.using(Configuration.builder()
                .options(Option.AS_PATH_LIST).build()).parse(jsonContent);

        SortedSet<String> encryptPaths = Sets.newTreeSet();
        for (String encryptParam : jsonPathPatterns) {
            if (JSON_PATH_ROOT.contains(encryptParam)) {
                return TOTAL_ENCRYPT_PARAMS;
            }
            if (encryptParam.startsWith(JSON_PATH_PREFIX)) {
                List<String> pathList = pathReadCtx.read(encryptParam);
                if (CollectionUtils.isNotEmpty(pathList)) {
                    encryptPaths.addAll(pathList);
                }
            }
        }
        encryptPaths.forEach(LOGGER::debug);
        return encryptPaths;
    }
}
