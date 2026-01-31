package com.hunterai.controller;

import com.hunterai.dto.ResumeResponse;
import com.hunterai.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ResumeController {
    
    private final ResumeService service;
    
    @PostMapping("/upload")
    public Mono<ResumeResponse> uploadResume(@RequestParam("file") MultipartFile file) {
        return service.uploadResume(file);
    }
    
    @GetMapping("/{id}")
    public Mono<ResumeResponse> getResume(@PathVariable Long id) {
        return service.getResume(id);
    }
    
    @PostMapping("/{resumeId}/optimize/{jobDescriptionId}")
    public Mono<ResumeResponse> optimizeResume(
            @PathVariable Long resumeId,
            @PathVariable Long jobDescriptionId) {
        return service.optimizeResume(resumeId, jobDescriptionId);
    }
}

