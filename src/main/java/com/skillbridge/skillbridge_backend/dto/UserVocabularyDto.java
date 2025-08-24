package com.skillbridge.skillbridge_backend.dto;

import com.skillbridge.skillbridge_backend.entity.UserVocabulary;
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
    }
}
