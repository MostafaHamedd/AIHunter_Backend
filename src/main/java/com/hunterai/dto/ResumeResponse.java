package com.hunterai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private String name;
    private ResumeContent originalContent;
    private ResumeContent optimizedContent;
    private String version;
    private LocalDateTime createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeContent {
        private String summary;
        private List<ExperienceDto> experience;
        private List<String> skills;
        private List<ProjectDto> projects;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceDto {
        private Long id;
        private String company;
        private String role;
        private String duration;
        private List<String> bullets;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectDto {
        private Long id;
        private String name;
        private String description;
        private List<String> technologies;
    }
}

