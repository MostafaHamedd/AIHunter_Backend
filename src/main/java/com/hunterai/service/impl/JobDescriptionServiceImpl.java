package com.hunterai.service.impl;

import com.hunterai.dto.JobDescriptionRequest;
import com.hunterai.dto.JobDescriptionResponse;
import com.hunterai.model.JobDescription;
import com.hunterai.repository.JobDescriptionRepository;
import com.hunterai.service.JobDescriptionService;
import com.hunterai.util.JsonUtil;
import com.hunterai.util.JobDescriptionScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDescriptionServiceImpl implements JobDescriptionService {
    
    private final JobDescriptionRepository repository;
    
    @Override
    public Mono<JobDescriptionResponse> analyzeJobDescription(JobDescriptionRequest request) {
        return Mono.fromCallable(() -> {
            JobDescription jobDescription = new JobDescription();
            jobDescription.setUrl(request.getUrl());
            jobDescription.setCreatedAt(LocalDateTime.now());
            
            List<String> requiredSkills;
            List<String> keywords;
            List<String> technologies;
            List<String> softSkills;
            List<String> responsibilities;
            
            // If URL is provided, scrape it
            if (request.getUrl() != null && !request.getUrl().trim().isEmpty()) {
                log.info("Scraping job description from URL: {}", request.getUrl());
                try {
                    JobDescriptionScraper.ScrapedJobData scrapedData = 
                        JobDescriptionScraper.scrapeJobDescription(request.getUrl());
                    
                    jobDescription.setTitle(scrapedData.title);
                    jobDescription.setCompany(scrapedData.company);
                    jobDescription.setDescription(scrapedData.description);
                    requiredSkills = scrapedData.requiredSkills;
                    keywords = scrapedData.keywords;
                    technologies = scrapedData.technologies;
                    softSkills = scrapedData.softSkills;
                    responsibilities = scrapedData.responsibilities;
                    
                    log.info("Successfully scraped job: {} at {}", scrapedData.title, scrapedData.company);
                } catch (Exception e) {
                    log.error("Error scraping job description, falling back to text parsing: {}", e.getMessage());
                    // Fallback to text parsing if scraping fails
                    jobDescription.setTitle("Job Position");
                    jobDescription.setCompany("Company");
                    jobDescription.setDescription(request.getText() != null ? request.getText() : "");
                    requiredSkills = extractFromText(request.getText());
                    keywords = extractKeywordsFromText(request.getText());
                    technologies = extractTechnologiesFromText(request.getText());
                    softSkills = extractSoftSkillsFromText(request.getText());
                    responsibilities = extractResponsibilitiesFromText(request.getText());
                }
            } else if (request.getText() != null && !request.getText().trim().isEmpty()) {
                // Parse from text
                log.info("Parsing job description from text");
                jobDescription.setTitle(extractTitleFromText(request.getText()));
                jobDescription.setCompany(extractCompanyFromText(request.getText()));
                jobDescription.setDescription(request.getText());
                requiredSkills = extractFromText(request.getText());
                keywords = extractKeywordsFromText(request.getText());
                technologies = extractTechnologiesFromText(request.getText());
                softSkills = extractSoftSkillsFromText(request.getText());
                responsibilities = extractResponsibilitiesFromText(request.getText());
            } else {
                // Fallback to defaults
                log.warn("No URL or text provided, using default values");
                jobDescription.setTitle("Job Position");
                jobDescription.setCompany("Company");
                jobDescription.setDescription("");
                requiredSkills = List.of();
                keywords = List.of();
                technologies = List.of();
                softSkills = List.of();
                responsibilities = List.of();
            }
            
            // Convert lists to JSON
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
            
            return jobDescription;
        })
        .flatMap(repository::save)
        .map(this::mapToResponse);
    }
    
    private String extractTitleFromText(String text) {
        // Look for common title patterns
        String[] patterns = {
            "Job Title:", "Position:", "Role:", "Title:"
        };
        for (String pattern : patterns) {
            int idx = text.indexOf(pattern);
            if (idx >= 0) {
                String title = text.substring(idx + pattern.length()).split("\n")[0].trim();
                if (title.length() < 100) {
                    return title;
                }
            }
        }
        return "Job Position";
    }
    
    private String extractCompanyFromText(String text) {
        // Look for company patterns
        String[] patterns = {
            "Company:", "Employer:", "Organization:"
        };
        for (String pattern : patterns) {
            int idx = text.indexOf(pattern);
            if (idx >= 0) {
                String company = text.substring(idx + pattern.length()).split("\n")[0].trim();
                if (company.length() < 100) {
                    return company;
                }
            }
        }
        return "Company";
    }
    
    private List<String> extractFromText(String text) {
        if (text == null) return List.of();
        return JobDescriptionScraper.scrapeJobDescription("").requiredSkills; // Use scraper's logic
    }
    
    private List<String> extractKeywordsFromText(String text) {
        if (text == null) return List.of();
        String lower = text.toLowerCase();
        List<String> keywords = new java.util.ArrayList<>();
        String[] common = {"full-stack", "remote", "senior", "junior", "agile", "scrum"};
        for (String kw : common) {
            if (lower.contains(kw)) {
                keywords.add(kw);
            }
        }
        return keywords;
    }
    
    private List<String> extractTechnologiesFromText(String text) {
        if (text == null) return List.of();
        String lower = text.toLowerCase();
        List<String> techs = new java.util.ArrayList<>();
        String[] common = {"react", "node.js", "python", "java", "aws", "docker", "kubernetes"};
        for (String tech : common) {
            if (lower.contains(tech)) {
                techs.add(tech);
            }
        }
        return techs;
    }
    
    private List<String> extractSoftSkillsFromText(String text) {
        if (text == null) return List.of();
        String lower = text.toLowerCase();
        List<String> skills = new java.util.ArrayList<>();
        String[] common = {"communication", "teamwork", "leadership", "problem-solving"};
        for (String skill : common) {
            if (lower.contains(skill)) {
                skills.add(skill);
            }
        }
        return skills;
    }
    
    private List<String> extractResponsibilitiesFromText(String text) {
        if (text == null) return List.of();
        List<String> responsibilities = new java.util.ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if ((line.startsWith("-") || line.startsWith("•") || line.matches("^\\d+[.)]\\s.*")) 
                && line.length() > 10 && line.length() < 200) {
                responsibilities.add(line.replaceFirst("^[-•]\\s*", "").replaceFirst("^\\d+[.)]\\s*", ""));
                if (responsibilities.size() >= 10) break;
            }
        }
        return responsibilities;
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

