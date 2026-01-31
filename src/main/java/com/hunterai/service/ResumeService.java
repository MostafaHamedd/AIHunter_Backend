package com.hunterai.service;

import com.hunterai.dto.ResumeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {
    reactor.core.publisher.Mono<ResumeResponse> uploadResume(MultipartFile file);
    reactor.core.publisher.Mono<ResumeResponse> getResume(Long id);
    reactor.core.publisher.Mono<ResumeResponse> optimizeResume(Long resumeId, Long jobDescriptionId);
}

