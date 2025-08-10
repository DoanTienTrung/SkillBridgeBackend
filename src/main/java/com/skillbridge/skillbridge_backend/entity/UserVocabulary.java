package com.skillbridge.skillbridge_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_vocabulary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vocabulary_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "is_learned")
    private Boolean isLearned = false;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}