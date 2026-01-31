package com.hunterai.repository;

import com.hunterai.model.CoverLetter;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoverLetterRepository extends R2dbcRepository<CoverLetter, Long> {
}

