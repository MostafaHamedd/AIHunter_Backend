package com.hunterai.repository;

import com.hunterai.model.Project;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProjectRepository extends R2dbcRepository<Project, Long> {
    Flux<Project> findByResumeId(Long resumeId);
}

