package my.javacraft.xsd2model.services;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Provide services for serialization and deserialization from Json to Object and visa versa.
 * ObjectMapper is 100% thread safe.
 */
public class JsonServices {

    // I don't like static initialization
    private static final ObjectMapper mapper = ((Supplier<ObjectMapper>) () -> {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }).get();

    public static String objectToJson(Object tag) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tag);
    }

    public static Object jsonToObject(String json, Class className) throws IOException {
        return mapper.readValue(json, className);
    }

    public static boolean isJson(String json) {
        try {
            if (json.contains("{") && json.contains("}")) {
                final ObjectMapper mapper = new ObjectMapper();
                mapper.readTree(json);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }
}