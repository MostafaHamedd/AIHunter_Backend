package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Table("job_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {
    
    @Id
    private Long id;
    
    @Column("company")
    private String company;
    
    @Column("role")
    private String role;
    
    @Column("job_link")
    private String jobLink;
    
    @Column("resume_id")
    private Long resumeId;
    
    @Column("cover_letter_id")
    private Long coverLetterId;
    
    @Column("status")
    private ApplicationStatus status;
    
    @Column("application_date")
    private LocalDateTime applicationDate;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    // Transient fields for easier access
    private transient List<ApplicationNote> notes;
    private transient List<TimelineEvent> timeline;
    
    public enum ApplicationStatus {
        NOT_APPLIED,
        APPLIED,
        INTERVIEW,
        OFFER,
        REJECTED
    }
}

