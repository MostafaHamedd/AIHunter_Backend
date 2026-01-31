package com.hunterai.service.impl;

import com.hunterai.dto.JobDescriptionRequest;
import com.hunterai.dto.JobDescriptionResponse;
import com.hunterai.model.JobDescription;
import com.hunterai.repository.JobDescriptionRepository;
import com.hunterai.service.JobDescriptionService;
import com.hunterai.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobDescriptionServiceImpl implements JobDescriptionService {
    
    private final JobDescriptionRepository repository;
    
    @Override
    public Mono<JobDescriptionResponse> analyzeJobDescription(JobDescriptionRequest request) {
        // TODO: Implement actual job description parsing and extraction
        // For now, return mock data
        JobDescription jobDescription = new JobDescription();
        jobDescription.setUrl(request.getUrl());
        jobDescription.setTitle("Software Engineer"); // Would be extracted
        jobDescription.setCompany("Tech Company"); // Would be extracted
        jobDescription.setDescription(request.getText());
        jobDescription.setCreatedAt(LocalDateTime.now());
        
        // Convert lists to JSON
        List<String> requiredSkills = Arrays.asList("React", "TypeScript", "Node.js", "REST APIs");
        List<String> keywords = Arrays.asList("frontend", "full-stack", "agile", "scrum");
        List<String> technologies = Arrays.asList("React", "TypeScript", "Node.js", "PostgreSQL", "AWS");
        List<String> softSkills = Arrays.asList("communication", "teamwork", "problem-solving");
        List<String> responsibilities = Arrays.asList(
            "Develop and maintain web applications",
            "Collaborate with cross-functional teams",
            "Write clean, maintainable code"
        );
        
        jobDescription.setRequiredSkillsJson(JsonUtil.toJson(requiredSkills));
        jobDescription.setKeywordsJson(JsonUtil.toJson(keywords));
        jobDescription.setTechnologiesJson(JsonUtil.toJson(technologies));
        jobDescription.setSoftSkillsJson(JsonUtil.toJson(softSkills));
        jobDescription.setResponsibilitiesJson(JsonUtil.toJson(responsibilities));
        
        // Set transient fields for mapping
        jobDescription.setRequiredSkills(requiredSkills);
        jobDescription.setKeywords(keywords);
        jobDescription.setTechnologies(technologies);
        jobDescription.setSoftSkills(softSkills);
        jobDescription.setResponsibilities(responsibilities);
        
        return repository.save(jobDescription)
            .map(this::mapToResponse);
    }
    
    @Override
    public Mono<JobDescriptionResponse> getJobDescription(Long id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Job description not found")))
            .map(this::mapToResponse);
    }
    
    private JobDescriptionResponse mapToResponse(JobDescription jobDescription) {
        JobDescriptionResponse response = new JobDescriptionResponse();
        response.setId(jobDescription.getId());
        response.setUrl(jobDescription.getUrl());
        response.setTitle(jobDescription.getTitle());
        response.setCompany(jobDescription.getCompany());
        response.setDescription(jobDescription.getDescription());
        response.setCreatedAt(jobDescription.getCreatedAt());
        
        JobDescriptionResponse.ExtractedData extractedData = new JobDescriptionResponse.ExtractedData();
        // Use transient fields if available, otherwise parse from JSON
        if (jobDescription.getRequiredSkills() != null) {
            extractedData.setRequiredSkills(jobDescription.getRequiredSkills());
        } else {
            extractedData.setRequiredSkills(JsonUtil.fromJson(jobDescription.getRequiredSkillsJson()));
        }
        
        if (jobDescription.getKeywords() != null) {
            extractedData.setKeywords(jobDescription.getKeywords());
        } else {
            extractedData.setKeywords(JsonUtil.fromJson(jobDescription.getKeywordsJson()));
        }
        
        if (jobDescription.getTechnologies() != null) {
            extractedData.setTechnologies(jobDescription.getTechnologies());
        } else {
            extractedData.setTechnologies(JsonUtil.fromJson(jobDescription.getTechnologiesJson()));
        }
        
        if (jobDescription.getSoftSkills() != null) {
            extractedData.setSoftSkills(jobDescription.getSoftSkills());
        } else {
            extractedData.setSoftSkills(JsonUtil.fromJson(jobDescription.getSoftSkillsJson()));
        }
        
        if (jobDescription.getResponsibilities() != null) {
            extractedData.setResponsibilities(jobDescription.getResponsibilities());
        } else {
            extractedData.setResponsibilities(JsonUtil.fromJson(jobDescription.getResponsibilitiesJson()));
        }
        
        response.setExtractedData(extractedData);
        
        return response;
    }
}

