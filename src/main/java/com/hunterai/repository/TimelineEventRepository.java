package com.hunterai.repository;

import com.hunterai.model.TimelineEvent;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TimelineEventRepository extends R2dbcRepository<TimelineEvent, Long> {
    Flux<TimelineEvent> findByApplicationId(Long applicationId);
}

