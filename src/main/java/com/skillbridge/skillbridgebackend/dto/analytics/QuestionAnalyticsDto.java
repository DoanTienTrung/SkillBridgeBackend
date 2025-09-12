package com.skillbridge.skillbridgebackend.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnalyticsDto {
    private Long questionId;
    private String questionText;
    private String questionType;
    private Integer totalAnswers;
    private Integer correctAnswers;
    private Double accuracyRate;
    private String correctAnswer;
    private String mostSelectedWrongAnswer;
}
