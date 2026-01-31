package com.hunterai.controller;

import com.hunterai.dto.JobDescriptionRequest;
import com.hunterai.dto.JobDescriptionResponse;
import com.hunterai.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/job-descriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class JobDescriptionController {
    
    private final JobDescriptionService service;
    
    @PostMapping("/analyze")
    public Mono<JobDescriptionResponse> analyzeJobDescription(
            @RequestBody JobDescriptionRequest request) {
        return service.analyzeJobDescription(request);
    }
    
    @GetMapping("/{id}")
    public Mono<JobDescriptionResponse> getJobDescription(@PathVariable Long id) {
        return service.getJobDescription(id);
    }
}

