package com.hunterai.repository;

import com.hunterai.model.JobDescription;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobDescriptionRepository extends R2dbcRepository<JobDescription, Long> {
}

