package com.hunterai.repository;

import com.hunterai.model.Resume;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends R2dbcRepository<Resume, Long> {
}

