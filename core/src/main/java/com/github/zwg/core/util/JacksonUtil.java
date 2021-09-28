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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class JacksonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JacksonUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .configure(MapperFeature.USE_ANNOTATIONS, true)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            logger.warn("transform String to Object failed. input:{},type:{}", json, type, e);
        }
        return null;
    }

    public static <T> List<T> fromJsonArray(String json, Class<T> type) {
        try {
            CollectionType javaType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, type);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.warn("transform String to Object Array failed. input:{},type:{}", json, type, e);
        }
        return null;
    }


    public static <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.warn("transform Data to String failed. input:{}", object, e);
        }
        return null;
    }

    public static <T> String toJsonPretty(T object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);
        } catch (IOException e) {
            logger.warn("transform Data to Pretty String failed. input:{}", object, e);
        }
        return null;
    }


}