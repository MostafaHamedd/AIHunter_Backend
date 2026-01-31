package com.hunterai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptionResponse {
    private Long id;
    private String url;
    private String title;
    private String company;
    private String description;
    private ExtractedData extractedData;
    private LocalDateTime createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedData {
        private List<String> requiredSkills;
        private List<String> keywords;
        private List<String> technologies;
        private List<String> softSkills;
        private List<String> responsibilities;
    }
}

