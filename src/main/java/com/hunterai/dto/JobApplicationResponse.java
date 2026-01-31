package com.hunterai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponse {
    private Long id;
    private String company;
    private String role;
    private String jobLink;
    private Long resumeId;
    private Long coverLetterId;
    private String status;
    private LocalDateTime applicationDate;
    private List<NoteDto> notes;
    private List<TimelineEventDto> timeline;
    private LocalDateTime createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteDto {
        private Long id;
        private String content;
        private LocalDateTime createdAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEventDto {
        private Long id;
        private String type;
        private String title;
        private String description;
        private LocalDateTime date;
    }
}

