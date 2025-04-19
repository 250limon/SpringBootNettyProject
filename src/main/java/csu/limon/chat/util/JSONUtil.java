package csu.limon.chat.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;

public class JSONUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectWriter writer = mapper.writer();

    public static byte[] toJsonBytes(Object obj) throws IOException {
        return mapper.writeValueAsBytes(obj);
    }

    public static String toJsonString(Object obj) throws IOException {
        return writer.writeValueAsString(obj);
    }

    public static <T> T parse(byte[] bytes, Class<T> clazz) throws IOException {
        return mapper.readValue(bytes, clazz);
    }

    public static <T> T parse(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }
}