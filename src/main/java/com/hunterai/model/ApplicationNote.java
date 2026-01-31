package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table("application_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationNote {
    
    @Id
    private Long id;
    
    @Column("application_id")
    private Long applicationId;
    
    @Column("content")
    private String content;
    
    @Column("created_at")
    private LocalDateTime createdAt;
}

