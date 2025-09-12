package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LessonDto {
    private Long id;
    private String title;
    private String description;
    private String level;
    private String type; // "listening" or "reading"
    private CategoryDto category;
    
    // For listening lessons
    private String audioUrl;
    private String transcript;
    private Integer durationSeconds;
    
    // For reading lessons
    private String content;
    private Integer wordCount;
    
    private LocalDateTime createdAt;
}
