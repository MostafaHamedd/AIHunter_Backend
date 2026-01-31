package com.hunterai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ATSScoreResponse {
    private int score;
    private List<KeywordMatch> matchedKeywords;
    private List<KeywordMatch> missingKeywords;
    private List<KeywordMatch> suggestedKeywords;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordMatch {
        private String keyword;
        private boolean matched;
        private boolean suggested;
        private String category;
    }
}

