package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonVocabularyDto {
    private Long id;
    private String word;
    private String phonetic;
    private String meaning;
    private String exampleSentence;
    private Integer startPosition;
    private Integer endPosition;
    private String highlightColor;
    private String selectedText;
}