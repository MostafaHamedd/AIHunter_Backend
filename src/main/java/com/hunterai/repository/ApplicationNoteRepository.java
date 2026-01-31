package com.hunterai.repository;

import com.hunterai.model.ApplicationNote;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ApplicationNoteRepository extends R2dbcRepository<ApplicationNote, Long> {
    Flux<ApplicationNote> findByApplicationId(Long applicationId);
}

