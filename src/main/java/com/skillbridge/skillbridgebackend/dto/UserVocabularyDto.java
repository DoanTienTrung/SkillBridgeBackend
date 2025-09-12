package com.skillbridge.skillbridgebackend.dto;

import com.skillbridge.skillbridgebackend.entity.UserVocabulary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserVocabularyDto {
    private Long id;
    private String word;
    private String phonetic;
    private String meaning;
    private String exampleSentence;
    private String status;
    private String statusDisplayName;
    private Boolean isLearned;
    private Integer reviewCount;
    private LocalDateTime lastReviewed;
    private LocalDateTime createdAt;
    
    // New fields
    private String category;
    private String difficulty;
    private String difficultyDisplayName;
    private String partOfSpeech;
    private String partOfSpeechDisplayName;
    private String synonyms;
    private String antonyms;
    private String notes;

    public UserVocabularyDto(UserVocabulary userVocabulary) {
        this.id = userVocabulary.getId();
        this.word = userVocabulary.getVocabulary().getWord();
        this.phonetic = userVocabulary.getVocabulary().getPhonetic();
        this.meaning = userVocabulary.getVocabulary().getMeaning();
        this.exampleSentence = userVocabulary.getVocabulary().getExampleSentence();
        this.status = userVocabulary.getStatus() != null ? userVocabulary.getStatus().name() : UserVocabulary.Status.LEARNING.name();
        this.statusDisplayName = userVocabulary.getStatus() != null ? userVocabulary.getStatus().getDisplayName() : UserVocabulary.Status.LEARNING.getDisplayName();
        this.isLearned = userVocabulary.getIsLearned();
        this.reviewCount = userVocabulary.getReviewCount();
        this.lastReviewed = userVocabulary.getLastReviewed();
        this.createdAt = userVocabulary.getCreatedAt();
        
        // New fields
        this.category = userVocabulary.getVocabulary().getCategory();
        this.difficulty = userVocabulary.getVocabulary().getDifficulty() != null ? userVocabulary.getVocabulary().getDifficulty().name() : null;
        this.difficultyDisplayName = userVocabulary.getVocabulary().getDifficulty() != null ? userVocabulary.getVocabulary().getDifficulty().getDisplayName() : null;
        this.partOfSpeech = userVocabulary.getVocabulary().getPartOfSpeech() != null ? userVocabulary.getVocabulary().getPartOfSpeech().name() : null;
        this.partOfSpeechDisplayName = userVocabulary.getVocabulary().getPartOfSpeech() != null ? userVocabulary.getVocabulary().getPartOfSpeech().getDisplayName() : null;
        this.synonyms = userVocabulary.getVocabulary().getSynonyms();
        this.antonyms = userVocabulary.getVocabulary().getAntonyms();
        this.notes = userVocabulary.getVocabulary().getNotes();
    }
}
