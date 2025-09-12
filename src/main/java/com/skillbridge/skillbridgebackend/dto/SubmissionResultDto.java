package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;

@Data
public class SubmissionResultDto {
    private Double score;
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer timeSpent;
}
