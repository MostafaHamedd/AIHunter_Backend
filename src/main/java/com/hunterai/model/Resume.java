package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Table("resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    
    @Id
    private Long id;
    
    @Column("name")
    private String name;
    
    @Column("summary")
    private String summary;
    
    @Column("skills")
    private String skillsJson; // Store as JSON string
    
    @Column("version")
    private String version;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    // Transient fields for easier access
    private transient List<Experience> experiences;
    private transient List<String> skills;
    private transient List<Project> projects;
}

