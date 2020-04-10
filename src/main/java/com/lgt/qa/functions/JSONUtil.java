package com.lgt.qa.functions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public final class JSONUtil {
    private static final Logger logger = LoggerFactory.getLogger(JSONUtil.class);
    private final static ObjectMapper objectMapper;
    private final static ObjectReader objectReader;
    private final static ObjectWriter objectWriter;
    private final static JsonFactory jsonFactory;

    static {
        objectMapper = new ObjectMapper();
        //objectMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES,false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectReader = objectMapper.reader();
        objectWriter = objectMapper.writer();
        jsonFactory = objectMapper.getFactory();
    }

    /**
     * json字符串反序列化成对象
     *
     * @param json json字符串
     * @param type 反序列化类型
     * @param <T>  要反序列化的类型
     * @return 反序列化得到的对象
     */
    public static <T> T unSerialize(String json, Type type) {
        try {
            return objectReader.readValue(jsonFactory.createParser(json), objectMapper.constructType(type));
        } catch (IOException e) {
            logger.error(String.format("Failed to convert json <%s> to object. ", json), e);
            return null;
        }
    }

    /**
     * json字符串反序列化成对象
     *
     * @param json json字符串
     * @param type 反序列化类型
     * @param <T>  要反序列化的类型
     * @return 反序列化得到的对象
     */
    public static <T> T unSerialize(String json, Class<T> type) {
        try {
            return objectReader.readValue(jsonFactory.createParser(json), type);
        } catch (IOException e) {
            logger.error(String.format("Failed to convert json <%s> to object. ", json), e);
            return null;
        }
    }

    /**
     * json字符串反序列化成对象
     *
     * @param json json字符串
     * @return JsonNode
     */
    public static JsonNode unSerialize(String json) {
        try {
            return objectReader.readTree(json);
        } catch (IOException e) {
            logger.error(String.format("Failed to convert json <%s> to object. ", json), e);
            return null;
        }
    }

    /**
     * json UTF-8 数组反序列化成对象
     *
     * @param json json UTF-8 数组
     * @param type 反序列化类型
     * @param <T>  要反序列化的类型
     * @return 反序列化得到的对象
     */
    public static <T> T unSerialize(byte[] json, Type type) {
        try {
            return objectReader.readValue(jsonFactory.createParser(json), objectMapper.constructType(type));
        } catch (IOException e) {
            logger.error(String.format("Failed to convert json <%s> to object. ", new String(json)), e);
            return null;
        }
    }

    /**
     * json UTF-8 数组反序列化成对象
     *
     * @param json json UTF-8 数组
     * @param type 反序列化类型
     * @param <T>  要反序列化的类型
     * @return 反序列化得到的对象
     */
    public static <T> T unSerialize(byte[] json, Class<T> type) {
        try {
            return objectReader.readValue(jsonFactory.createParser(json), type);
        } catch (IOException e) {
            logger.error(String.format("Failed to convert json <%s> to object. ", new String(json)), e);
            return null;
        }
    }

    /**
     * json UTF-8 数组反序列化成对象
     *
     * @param json json UTF-8 数组
     * @return JsonNode
     */
    public static JsonNode unSerialize(byte[] json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            logger.error(String.format("Failed to convert json <%s> to object. ", new String(json)), e);
            return null;
        }
    }

    /**
     * 创建JsonObjectNode
     *
     * @return ObjectNode
     */
    public static ObjectNode createJsonObjectNode() {
        return (ObjectNode) objectReader.createObjectNode();
    }

    /**
     * 创建JsonArrayNode
     *
     * @return ArrayNode
     */
    public static ArrayNode createJsonArrayNode() {
        return (ArrayNode) objectReader.createArrayNode();
    }

    /**
     * 对象序列化成json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public static String serialize(Object obj) {
        try {
            return objectWriter.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to json. ", e);
            return null;
        }
    }

    /**
     * 对象序列化成json UTF-8 数组
     *
     * @param obj 对象
     * @return json UTF-8 数组
     */
    public static byte[] serializeToBytes(Object obj) {
        try {
            return objectWriter.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to json. ", e);
            return null;
        }
    }
}
