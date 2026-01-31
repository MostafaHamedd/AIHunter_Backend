package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table("cover_letters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetter {
    
    @Id
    private Long id;
    
    @Column("name")
    private String name;
    
    @Column("original_content")
    private String originalContent;
    
    @Column("optimized_content")
    private String optimizedContent;
    
    @Column("version")
    private String version;
    
    @Column("created_at")
    private LocalDateTime createdAt;
}

