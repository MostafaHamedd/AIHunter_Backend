package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Table("job_descriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDescription {
    
    @Id
    private Long id;
    
    private String url;
    
    @Column("title")
    private String title;
    
    @Column("company")
    private String company;
    
    @Column("description")
    private String description;
    
    @Column("required_skills")
    private String requiredSkillsJson; // Store as JSON string
    
    @Column("keywords")
    private String keywordsJson; // Store as JSON string
    
    @Column("technologies")
    private String technologiesJson; // Store as JSON string
    
    @Column("soft_skills")
    private String softSkillsJson; // Store as JSON string
    
    @Column("responsibilities")
    private String responsibilitiesJson; // Store as JSON string
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    // Transient fields for easier access (will be converted to/from JSON)
    private transient List<String> requiredSkills;
    private transient List<String> keywords;
    private transient List<String> technologies;
    private transient List<String> softSkills;
    private transient List<String> responsibilities;
}

