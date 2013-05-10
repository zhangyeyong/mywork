package com.zte.jbundle.api;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonParser {

    public static String toJson(Object o) {
        if (o == null) {
            return "";
        }

        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
