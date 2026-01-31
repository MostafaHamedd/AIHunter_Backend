package com.hunterai.service;

import com.hunterai.dto.ATSScoreResponse;

public interface ATSService {
    reactor.core.publisher.Mono<ATSScoreResponse> calculateScore(Long resumeId, Long jobDescriptionId);
}

