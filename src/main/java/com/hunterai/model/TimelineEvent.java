package com.hunterai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table("timeline_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {
    
    @Id
    private Long id;
    
    @Column("application_id")
    private Long applicationId;
    
    @Column("type")
    private EventType type;
    
    @Column("title")
    private String title;
    
    @Column("description")
    private String description;
    
    @Column("date")
    private LocalDateTime date;
    
    public enum EventType {
        OPTIMIZED,
        SUBMITTED,
        STATUS_CHANGE,
        NOTE
    }
}

