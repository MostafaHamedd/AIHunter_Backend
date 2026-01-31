package com.hunterai.service;

import com.hunterai.dto.JobDescriptionRequest;
import com.hunterai.dto.JobDescriptionResponse;
import reactor.core.publisher.Mono;

public interface JobDescriptionService {
    Mono<JobDescriptionResponse> analyzeJobDescription(JobDescriptionRequest request);
    Mono<JobDescriptionResponse> getJobDescription(Long id);
}

