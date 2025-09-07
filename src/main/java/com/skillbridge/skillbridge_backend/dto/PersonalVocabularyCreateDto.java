package com.skillbridge.skillbridge_backend.dto;

import com.skillbridge.skillbridge_backend.entity.Vocabulary;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalVocabularyCreateDto {

    @NotBlank(message = "Từ vựng không được để trống")
    private String word;

    private String phonetic;

    @NotBlank(message = "Nghĩa của từ không được để trống")
    private String meaning;

    private String exampleSentence;
    
    private String category;
    
    private Vocabulary.Difficulty difficulty;
    
    private Vocabulary.PartOfSpeech partOfSpeech;
    
    private String synonyms;
    
    private String antonyms;
    
    private String notes;
}
