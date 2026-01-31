package com.hunterai.controller;

import com.hunterai.dto.ATSScoreResponse;
import com.hunterai.service.ATSService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ATSController {
    
    private final ATSService service;
    
    @GetMapping("/score")
    public Mono<ATSScoreResponse> calculateScore(
            @RequestParam Long resumeId,
            @RequestParam Long jobDescriptionId) {
        return service.calculateScore(resumeId, jobDescriptionId);
    }
}

