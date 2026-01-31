package com.hunterai.service.impl;

import com.hunterai.dto.JobApplicationResponse;
import com.hunterai.model.ApplicationNote;
import com.hunterai.model.JobApplication;
import com.hunterai.model.JobDescription;
import com.hunterai.model.Resume;
import com.hunterai.model.TimelineEvent;
import com.hunterai.repository.ApplicationNoteRepository;
import com.hunterai.repository.JobApplicationRepository;
import com.hunterai.repository.JobDescriptionRepository;
import com.hunterai.repository.ResumeRepository;
import com.hunterai.repository.TimelineEventRepository;
import com.hunterai.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {
    
    private final JobApplicationRepository repository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationNoteRepository noteRepository;
    private final TimelineEventRepository timelineRepository;
    
    @Override
    public Mono<JobApplicationResponse> createApplication(Long jobDescriptionId, Long resumeId) {
        return Mono.zip(
            jobDescriptionRepository.findById(jobDescriptionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Job description not found"))),
            resumeRepository.findById(resumeId)
                .switchIfEmpty(Mono.error(new RuntimeException("Resume not found")))
        )
        .flatMap(tuple -> {
            JobDescription jobDescription = tuple.getT1();
            Resume resume = tuple.getT2();
            
            JobApplication application = new JobApplication();
            application.setCompany(jobDescription.getCompany());
            application.setRole(jobDescription.getTitle());
            application.setJobLink(jobDescription.getUrl());
            application.setResumeId(resumeId);
            application.setStatus(JobApplication.ApplicationStatus.NOT_APPLIED);
            application.setCreatedAt(LocalDateTime.now());
            
                return repository.save(application)
                    .flatMap(savedApp -> {
                        // Create initial timeline event
                        TimelineEvent event = new TimelineEvent();
                        event.setApplicationId(savedApp.getId());
                        event.setType(TimelineEvent.EventType.OPTIMIZED);
                        event.setTitle("Resume Optimized");
                        event.setDescription("Resume optimized for " + jobDescription.getCompany() + " - " + jobDescription.getTitle());
                        event.setDate(LocalDateTime.now());
                        
                        return timelineRepository.save(event)
                            .thenReturn(savedApp);
                    })
                    .flatMap(app -> loadApplicationWithRelations(app))
                    .map(this::mapToResponse);
        });
    }
    
    @Override
    public Mono<JobApplicationResponse> getApplication(Long id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Application not found")))
            .flatMap(this::loadApplicationWithRelations)
            .map(this::mapToResponse);
    }
    
    @Override
    public Flux<JobApplicationResponse> getAllApplications() {
        return repository.findAll()
            .flatMap(this::loadApplicationWithRelations)
            .map(this::mapToResponse);
    }
    
    @Override
    public Flux<JobApplicationResponse> searchApplications(String searchTerm) {
        return repository.findByCompanyContainingIgnoreCaseOrRoleContainingIgnoreCase(searchTerm)
            .flatMap(this::loadApplicationWithRelations)
            .map(this::mapToResponse);
    }
    
    @Override
    public Flux<JobApplicationResponse> filterByStatus(JobApplication.ApplicationStatus status) {
        return repository.findByStatus(status)
            .flatMap(this::loadApplicationWithRelations)
            .map(this::mapToResponse);
    }
    
    @Override
    public Mono<JobApplicationResponse> updateApplicationStatus(Long id, JobApplication.ApplicationStatus status) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Application not found")))
            .flatMap(application -> {
                application.setStatus(status);
                if (status == JobApplication.ApplicationStatus.APPLIED && application.getApplicationDate() == null) {
                    application.setApplicationDate(LocalDateTime.now());
                }
                
                return repository.save(application)
                    .flatMap(savedApp -> {
                        // Create timeline event
                        TimelineEvent event = new TimelineEvent();
                        event.setApplicationId(savedApp.getId());
                        event.setType(TimelineEvent.EventType.STATUS_CHANGE);
                        event.setTitle("Status changed to " + status.name().toLowerCase().replace("_", " "));
                        event.setDescription("Application status updated");
                        event.setDate(LocalDateTime.now());
                        
                        return timelineRepository.save(event)
                            .thenReturn(savedApp);
                    })
                    .flatMap(app -> loadApplicationWithRelations(app))
                    .map(this::mapToResponse);
            });
    }
    
    @Override
    public Mono<JobApplicationResponse> addNote(Long id, String note) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Application not found")))
            .flatMap(application -> {
                // Create note
                ApplicationNote applicationNote = new ApplicationNote();
                applicationNote.setApplicationId(application.getId());
                applicationNote.setContent(note);
                applicationNote.setCreatedAt(LocalDateTime.now());
                
                return noteRepository.save(applicationNote)
                    .flatMap(savedNote -> {
                        // Create timeline event
                        TimelineEvent event = new TimelineEvent();
                        event.setApplicationId(application.getId());
                        event.setType(TimelineEvent.EventType.NOTE);
                        event.setTitle("Note added");
                        event.setDescription(note);
                        event.setDate(LocalDateTime.now());
                        
                        return timelineRepository.save(event)
                            .then(Mono.defer(() -> Mono.just(application)));
                    })
                    .flatMap(app -> loadApplicationWithRelations((JobApplication) app))
                    .map(this::mapToResponse);
            });
    }
    
    private Mono<JobApplication> loadApplicationWithRelations(JobApplication application) {
        return Mono.zip(
            noteRepository.findByApplicationId(application.getId()).collectList(),
            timelineRepository.findByApplicationId(application.getId()).collectList()
        )
        .map(tuple -> {
            application.setNotes(tuple.getT1());
            application.setTimeline(tuple.getT2());
            return application;
        });
    }
    
    private JobApplicationResponse mapToResponse(JobApplication application) {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(application.getId());
        response.setCompany(application.getCompany());
        response.setRole(application.getRole());
        response.setJobLink(application.getJobLink());
        response.setResumeId(application.getResumeId());
        response.setCoverLetterId(application.getCoverLetterId());
        response.setStatus(application.getStatus().name());
        response.setApplicationDate(application.getApplicationDate());
        response.setCreatedAt(application.getCreatedAt());
        
        if (application.getNotes() != null) {
            response.setNotes(application.getNotes().stream()
                .map(note -> {
                    JobApplicationResponse.NoteDto dto = new JobApplicationResponse.NoteDto();
                    dto.setId(note.getId());
                    dto.setContent(note.getContent());
                    dto.setCreatedAt(note.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList()));
        }
        
        if (application.getTimeline() != null) {
            response.setTimeline(application.getTimeline().stream()
                .map(event -> {
                    JobApplicationResponse.TimelineEventDto dto = new JobApplicationResponse.TimelineEventDto();
                    dto.setId(event.getId());
                    dto.setType(event.getType().name());
                    dto.setTitle(event.getTitle());
                    dto.setDescription(event.getDescription());
                    dto.setDate(event.getDate());
                    return dto;
                })
                .collect(Collectors.toList()));
        }
        
        return response;
    }
}
