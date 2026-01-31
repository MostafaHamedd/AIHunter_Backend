package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Table("projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    
    @Id
    private Long id;
    
    @Column("resume_id")
    private Long resumeId;
    
    @Column("name")
    private String name;
    
    @Column("description")
    private String description;
    
    @Column("technologies")
    private String technologiesJson; // Store as JSON string
    
    // Transient field for easier access
    private transient List<String> technologies;
}

