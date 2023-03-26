package ru.hse.jade.sample.gson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;

public class MyGson {
    public static final com.google.gson.Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();
    public static <T> T fromJSONMapper(final TypeReference<T> type,
                                 final String jsonPacket) {
        T data = null;

        try {
            data = new ObjectMapper().readValue(jsonPacket, type);
        } catch (Exception e) {
            // Handle the problem
        }
        return data;
    }
}
