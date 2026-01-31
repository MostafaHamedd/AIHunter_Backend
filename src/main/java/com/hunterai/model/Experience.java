package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Table("experiences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Experience {
    
    @Id
    private Long id;
    
    @Column("resume_id")
    private Long resumeId;
    
    @Column("company")
    private String company;
    
    @Column("role")
    private String role;
    
    @Column("duration")
    private String duration;
    
    @Column("bullets")
    private String bulletsJson; // Store as JSON string
    
    // Transient field for easier access
    private transient List<String> bullets;
}

