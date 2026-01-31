package com.hunterai.repository;

import com.hunterai.model.Experience;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ExperienceRepository extends R2dbcRepository<Experience, Long> {
    Flux<Experience> findByResumeId(Long resumeId);
}

