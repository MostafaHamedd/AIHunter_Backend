package com.hunterai.controller;

import com.hunterai.dto.JobApplicationResponse;
import com.hunterai.model.JobApplication;
import com.hunterai.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class JobApplicationController {
    
    private final JobApplicationService service;
    
    @PostMapping
    public Mono<JobApplicationResponse> createApplication(
            @RequestBody Map<String, Long> request) {
        Long jobDescriptionId = request.get("jobDescriptionId");
        Long resumeId = request.get("resumeId");
        return service.createApplication(jobDescriptionId, resumeId);
    }
    
    @GetMapping
    public Flux<JobApplicationResponse> getAllApplications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        if (search != null && !search.isEmpty()) {
            return service.searchApplications(search);
        } else if (status != null && !status.isEmpty()) {
            return service.filterByStatus(JobApplication.ApplicationStatus.valueOf(status.toUpperCase()));
        } else {
            return service.getAllApplications();
        }
    }
    
    @GetMapping("/{id}")
    public Mono<JobApplicationResponse> getApplication(@PathVariable Long id) {
        return service.getApplication(id);
    }
    
    @PutMapping("/{id}/status")
    public Mono<JobApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        JobApplication.ApplicationStatus status = JobApplication.ApplicationStatus.valueOf(
            request.get("status").toUpperCase());
        return service.updateApplicationStatus(id, status);
    }
    
    @PostMapping("/{id}/notes")
    public Mono<JobApplicationResponse> addNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String note = request.get("note");
        return service.addNote(id, note);
    }
}

