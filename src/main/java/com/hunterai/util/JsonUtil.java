package com.hunterai.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("Error converting list to JSON: {}", e.getMessage(), e);
            return "[]";
        }
    }
    
    public static List<String> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error converting JSON to list: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}

