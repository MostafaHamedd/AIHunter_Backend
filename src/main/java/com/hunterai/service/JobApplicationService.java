package com.hunterai.service;

import com.hunterai.dto.JobApplicationResponse;
import com.hunterai.model.JobApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JobApplicationService {
    Mono<JobApplicationResponse> createApplication(Long jobDescriptionId, Long resumeId);
    Mono<JobApplicationResponse> getApplication(Long id);
    Flux<JobApplicationResponse> getAllApplications();
    Flux<JobApplicationResponse> searchApplications(String searchTerm);
    Flux<JobApplicationResponse> filterByStatus(JobApplication.ApplicationStatus status);
    Mono<JobApplicationResponse> updateApplicationStatus(Long id, JobApplication.ApplicationStatus status);
    Mono<JobApplicationResponse> addNote(Long id, String note);
}

