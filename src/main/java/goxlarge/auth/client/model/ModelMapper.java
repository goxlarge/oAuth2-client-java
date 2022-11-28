package goxlarge.auth.client.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelMapper {
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String serializeAuthzCodeGrant(AuthzCodeGrant value) {
        return serialize(value);
    }

    public static AuthzCodeGrant deserializeAuthzCodeGrant(String value) {
        return deserialize(value, AuthzCodeGrant.class);
    }

    private static String serialize(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object! " + String.valueOf(obj));
        }
    }

    private static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize object to " + String.valueOf(clazz) + " from: " + json);
        }
    }
}
