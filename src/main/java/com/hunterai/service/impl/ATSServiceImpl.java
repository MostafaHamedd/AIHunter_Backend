package com.hunterai.service.impl;

import com.hunterai.dto.ATSScoreResponse;
import com.hunterai.model.JobDescription;
import com.hunterai.model.Resume;
import com.hunterai.repository.ExperienceRepository;
import com.hunterai.repository.JobDescriptionRepository;
import com.hunterai.repository.ProjectRepository;
import com.hunterai.repository.ResumeRepository;
import com.hunterai.service.ATSService;
import com.hunterai.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ATSServiceImpl implements ATSService {
    
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    
    @Override
    public Mono<ATSScoreResponse> calculateScore(Long resumeId, Long jobDescriptionId) {
        return Mono.zip(
            resumeRepository.findById(resumeId)
                .switchIfEmpty(Mono.error(new RuntimeException("Resume not found"))),
            jobDescriptionRepository.findById(jobDescriptionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Job description not found")))
        )
        .flatMap(tuple -> {
            Resume resume = tuple.getT1();
            JobDescription jobDescription = tuple.getT2();
            
            // Load related entities
            return Mono.zip(
                experienceRepository.findByResumeId(resumeId).collectList(),
                projectRepository.findByResumeId(resumeId).collectList()
            )
            .map(relatedTuple -> {
                // Convert JSON fields to lists
                List<String> resumeSkills = JsonUtil.fromJson(resume.getSkillsJson());
                List<String> resumeKeywords = new ArrayList<>(resumeSkills);
                
                // Add experience bullets
                relatedTuple.getT1().forEach(exp -> {
                    List<String> bullets = JsonUtil.fromJson(exp.getBulletsJson());
                    resumeKeywords.addAll(bullets);
                });
                
                // Add project technologies
                relatedTuple.getT2().forEach(project -> {
                    List<String> technologies = JsonUtil.fromJson(project.getTechnologiesJson());
                    resumeKeywords.addAll(technologies);
                });
                
                // Collect job requirements
                List<String> jobKeywords = new ArrayList<>();
                if (jobDescription.getRequiredSkills() != null) {
                    jobKeywords.addAll(jobDescription.getRequiredSkills());
                } else {
                    jobKeywords.addAll(JsonUtil.fromJson(jobDescription.getRequiredSkillsJson()));
                }
                if (jobDescription.getKeywords() != null) {
                    jobKeywords.addAll(jobDescription.getKeywords());
                } else {
                    jobKeywords.addAll(JsonUtil.fromJson(jobDescription.getKeywordsJson()));
                }
                if (jobDescription.getTechnologies() != null) {
                    jobKeywords.addAll(jobDescription.getTechnologies());
                } else {
                    jobKeywords.addAll(JsonUtil.fromJson(jobDescription.getTechnologiesJson()));
                }
                
                // Calculate matches
                List<String> matched = jobKeywords.stream()
                    .filter(keyword -> resumeKeywords.stream()
                        .anyMatch(resumeKeyword -> resumeKeyword.toLowerCase().contains(keyword.toLowerCase())))
                    .collect(Collectors.toList());
                
                List<String> missing = jobKeywords.stream()
                    .filter(keyword -> !matched.contains(keyword))
                    .collect(Collectors.toList());
                
                // Calculate score
                int score = jobKeywords.isEmpty() ? 0 : (matched.size() * 100) / jobKeywords.size();
                
                ATSScoreResponse response = new ATSScoreResponse();
                response.setScore(score);
                response.setMatchedKeywords(matched.stream()
                    .map(kw -> createKeywordMatch(kw, true, false, "keyword"))
                    .collect(Collectors.toList()));
                response.setMissingKeywords(missing.stream()
                    .map(kw -> createKeywordMatch(kw, false, false, "keyword"))
                    .collect(Collectors.toList()));
                response.setSuggestedKeywords(Arrays.asList(
                    createKeywordMatch("REST APIs", false, true, "skill"),
                    createKeywordMatch("agile", false, true, "keyword")
                ));
                
                return response;
            });
        });
    }
    
    private ATSScoreResponse.KeywordMatch createKeywordMatch(String keyword, boolean matched, boolean suggested, String category) {
        ATSScoreResponse.KeywordMatch match = new ATSScoreResponse.KeywordMatch();
        match.setKeyword(keyword);
        match.setMatched(matched);
        match.setSuggested(suggested);
        match.setCategory(category);
        return match;
    }
}
