package com.yeepay.yop.sdk.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.utils.json.joda.DatetimeModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

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
    }

    private static final ObjectWriter WRITER = OBJECT_MAPPER.writer();
    private static final ObjectWriter PRETTY_WRITER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    public static String toJsonPrettyString(Object value) throws JsonProcessingException {
        return PRETTY_WRITER.writeValueAsString(value);
    }

    public static String toJsonString(Object value) {
        try {
            return WRITER.writeValueAsString(value);
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

    public static void load(InputStream input, Object obj) throws IOException, JsonProcessingException {
        OBJECT_MAPPER.readerForUpdating(obj).readValue(input);
    }

    public static void load(String content, Object obj) throws IOException, JsonProcessingException {
        OBJECT_MAPPER.readerForUpdating(obj).readValue(content);
    }

    public static <T> T loadFrom(InputStream input, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        return OBJECT_MAPPER.readValue(input, clazz);
    }

    public static <T> T loadFrom(String content, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        return OBJECT_MAPPER.readValue(content, clazz);
    }

    public static <T> T loadFromYAML(InputStream input, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        return YAML_OBJECT_MAPPER.readValue(input, clazz);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static ObjectWriter getWriter() {
        return WRITER;
    }

    public static ObjectWriter getPrettywriter() {
        return PRETTY_WRITER;
    }
}
