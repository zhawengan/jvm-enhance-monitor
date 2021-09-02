package com.github.zwg.core.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class JacksonObjectFormat {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BiFunction<String, IOException, Object> exceptionHandler;

    public JacksonObjectFormat() {
        this((cause, e) -> {
            throw new IllegalStateException(cause, e);
        });
    }

    public JacksonObjectFormat(
            BiFunction<String, IOException, Object> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        objectMapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            return handleEx("Failed to deserialize json: " + json, e);
        }
    }

    public <T> List<T> fromJsonArray(String json, Class<T> type) {
        try {
            CollectionType javaType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, type);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            return handleEx("Failed to deserialize json: " + json, e);
        }
    }


    public <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            return handleEx("Failed to serialize json: " + object, e);
        }
    }

    public <T> String toJsonPretty(T t) {
        try {
            String jsonStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(t);
            return jsonStr;
        } catch (IOException e) {
            return handleEx("Failed to serialize json pretty: " + t, e);
        }
    }


    private <T> T handleEx(String cause, IOException e) {
        return (T) exceptionHandler.apply(cause, e);
    }
}