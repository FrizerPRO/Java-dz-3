package ru.hse.jade.sample.gson;
import com.google.gson.GsonBuilder;

public class MyGson {
    public static final com.google.gson.Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();
}
