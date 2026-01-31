package com.hunterai.service.impl;

import com.hunterai.dto.ResumeResponse;
import com.hunterai.model.Experience;
import com.hunterai.model.Project;
import com.hunterai.model.Resume;
import com.hunterai.repository.ExperienceRepository;
import com.hunterai.repository.ProjectRepository;
import com.hunterai.repository.ResumeRepository;
import com.hunterai.service.ResumeService;
import com.hunterai.util.JsonUtil;
import com.hunterai.util.ResumeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {
    
    private final ResumeRepository resumeRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    
    @Override
    public Mono<ResumeResponse> uploadResume(MultipartFile file) {
        log.info("Parsing resume file: {}", file.getOriginalFilename());
        
        return Mono.fromCallable(() -> {
            try {
                return ResumeParser.parseResume(file.getInputStream(), file.getOriginalFilename());
            } catch (Exception e) {
                log.error("Error parsing resume file: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to parse resume: " + e.getMessage(), e);
            }
        })
        .flatMap(parsedResume -> {
            Resume resume = new Resume();
            resume.setName(file.getOriginalFilename());
            resume.setVersion("1.0");
            resume.setCreatedAt(LocalDateTime.now());
            
            // Set summary
            resume.setSummary(parsedResume.summary != null && !parsedResume.summary.isEmpty() 
                ? parsedResume.summary 
                : "Professional seeking new opportunities.");
            
            // Convert skills to JSON
            resume.setSkillsJson(JsonUtil.toJson(
                parsedResume.skills != null && !parsedResume.skills.isEmpty() 
                    ? parsedResume.skills 
                    : new ArrayList<>()
            ));
            
            // Save resume first
            return resumeRepository.save(resume)
                .flatMap(savedResume -> {
                    // Save experiences
                    Flux<Experience> experiencesFlux = Flux.fromIterable(
                        parsedResume.experiences != null ? parsedResume.experiences : new ArrayList<>()
                    )
                    .map(expData -> {
                        Experience experience = new Experience();
                        experience.setResumeId(savedResume.getId());
                        experience.setRole(expData.role != null ? expData.role : "Position");
                        experience.setCompany(expData.company != null ? expData.company : "Company");
                        experience.setDuration(expData.duration != null ? expData.duration : "");
                        experience.setBulletsJson(JsonUtil.toJson(
                            expData.bullets != null ? expData.bullets : new ArrayList<>()
                        ));
                        return experience;
                    })
                    .flatMap(experienceRepository::save);
                    
                    // Save projects
                    Flux<Project> projectsFlux = Flux.fromIterable(
                        parsedResume.projects != null ? parsedResume.projects : new ArrayList<>()
                    )
                    .map(projData -> {
                        Project project = new Project();
                        project.setResumeId(savedResume.getId());
                        project.setName(projData.name != null ? projData.name : "Project");
                        project.setDescription(projData.description != null ? projData.description : "");
                        project.setTechnologiesJson(JsonUtil.toJson(
                            projData.technologies != null ? projData.technologies : new ArrayList<>()
                        ));
                        return project;
                    })
                    .flatMap(projectRepository::save);
                    
                    // Wait for all related entities to be saved, then return the resume
                    return Flux.concat(experiencesFlux, projectsFlux)
                        .then(Mono.just(savedResume));
                })
                .flatMap(this::loadResumeWithRelations)
                .map(this::mapToResponse);
        })
        .onErrorResume(e -> {
            log.error("Error processing resume: {}", e.getMessage(), e);
            // Return a basic resume with error message
            Resume errorResume = new Resume();
            errorResume.setName(file.getOriginalFilename());
            errorResume.setVersion("1.0");
            errorResume.setCreatedAt(LocalDateTime.now());
            errorResume.setSummary("Unable to parse resume. Please ensure the file is a valid PDF or DOCX format.");
            errorResume.setSkillsJson("[]");
            
            return resumeRepository.save(errorResume)
                .flatMap(this::loadResumeWithRelations)
                .map(this::mapToResponse);
        });
    }
    
    @Override
    public Mono<ResumeResponse> getResume(Long id) {
        return resumeRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Resume not found")))
            .flatMap(this::loadResumeWithRelations)
            .map(this::mapToResponse);
    }
    
    @Override
    public Mono<ResumeResponse> optimizeResume(Long resumeId, Long jobDescriptionId) {
        return getResume(resumeId)
            .map(response -> {
                // TODO: Implement actual AI optimization
                // For now, just update the optimized content to match original
                response.setOptimizedContent(response.getOriginalContent());
                return response;
            });
    }
    
    private Mono<Resume> loadResumeWithRelations(Resume resume) {
        return Mono.zip(
            experienceRepository.findByResumeId(resume.getId()).collectList(),
            projectRepository.findByResumeId(resume.getId()).collectList()
        )
        .map(tuple -> {
            // Convert JSON fields to transient lists
            resume.setSkills(JsonUtil.fromJson(resume.getSkillsJson()));
            
            // Set experiences and convert their JSON fields
            resume.setExperiences(tuple.getT1().stream()
                .map(exp -> {
                    exp.setBullets(JsonUtil.fromJson(exp.getBulletsJson()));
                    return exp;
                })
                .collect(Collectors.toList()));
            
            // Set projects and convert their JSON fields
            resume.setProjects(tuple.getT2().stream()
                .map(proj -> {
                    proj.setTechnologies(JsonUtil.fromJson(proj.getTechnologiesJson()));
                    return proj;
                })
                .collect(Collectors.toList()));
            
            return resume;
        });
    }
    
    private ResumeResponse mapToResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setVersion(resume.getVersion());
        response.setCreatedAt(resume.getCreatedAt());
        
        ResumeResponse.ResumeContent originalContent = new ResumeResponse.ResumeContent();
        originalContent.setSummary(resume.getSummary());
        originalContent.setExperience(resume.getExperiences() != null ? resume.getExperiences().stream()
            .map(this::mapExperience)
            .collect(Collectors.toList()) : new ArrayList<>());
        originalContent.setSkills(resume.getSkills() != null ? resume.getSkills() : new ArrayList<>());
        originalContent.setProjects(resume.getProjects() != null ? resume.getProjects().stream()
            .map(this::mapProject)
            .collect(Collectors.toList()) : new ArrayList<>());
        
        response.setOriginalContent(originalContent);
        response.setOptimizedContent(originalContent); // For now, same as original
        
        return response;
    }
    
    private ResumeResponse.ExperienceDto mapExperience(Experience experience) {
        ResumeResponse.ExperienceDto dto = new ResumeResponse.ExperienceDto();
        dto.setId(experience.getId());
        dto.setCompany(experience.getCompany());
        dto.setRole(experience.getRole());
        dto.setDuration(experience.getDuration());
        dto.setBullets(experience.getBullets() != null ? experience.getBullets() : new ArrayList<>());
        return dto;
    }
    
    private ResumeResponse.ProjectDto mapProject(Project project) {
        ResumeResponse.ProjectDto dto = new ResumeResponse.ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setTechnologies(project.getTechnologies() != null ? project.getTechnologies() : new ArrayList<>());
        return dto;
    }
}
