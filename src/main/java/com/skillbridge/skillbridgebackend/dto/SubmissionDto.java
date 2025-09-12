package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SubmissionDto {
    private Long lessonId;
    private String lessonType; // "listening" or "reading"
    private Map<String, String> answers; // questionId -> selectedAnswer
    private Integer timeSpent; // in seconds
}
