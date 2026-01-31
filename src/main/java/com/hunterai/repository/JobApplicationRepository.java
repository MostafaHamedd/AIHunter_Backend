package com.hunterai.repository;

import com.hunterai.model.JobApplication;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface JobApplicationRepository extends R2dbcRepository<JobApplication, Long> {
    @Query("SELECT * FROM job_applications WHERE LOWER(company) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(role) LIKE LOWER(CONCAT('%', :search, '%'))")
    Flux<JobApplication> findByCompanyContainingIgnoreCaseOrRoleContainingIgnoreCase(String search);
    
    Flux<JobApplication> findByStatus(JobApplication.ApplicationStatus status);
}

