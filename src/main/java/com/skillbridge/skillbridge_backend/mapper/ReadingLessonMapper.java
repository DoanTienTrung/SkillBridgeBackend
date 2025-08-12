// Tạo file mapper/ReadingLessonMapper.java
package com.skillbridge.skillbridge_backend.mapper;

import com.skillbridge.skillbridge_backend.dto.ReadingLessonDto;
import com.skillbridge.skillbridge_backend.entity.ReadingLesson;

public class ReadingLessonMapper {

    public static ReadingLessonDto toDto(ReadingLesson lesson) {
        if (lesson == null) {
            return null;
        }

        ReadingLessonDto dto = new ReadingLessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setDescription(lesson.getDescription());
        dto.setLevel(lesson.getLevel().name());
        dto.setCategoryName(lesson.getCategory() != null ? lesson.getCategory().getName() : null);
        dto.setCategoryId(lesson.getCategory() != null ? lesson.getCategory().getId() : null);
        dto.setContent(lesson.getContent());
        dto.setWordCount(lesson.getWordCount());
        dto.setStatus(lesson.getStatus().name());
        dto.setCreatedBy(lesson.getCreatedBy() != null ? lesson.getCreatedBy().getEmail() : null);
        dto.setCreatedAt(lesson.getCreatedAt());
        dto.setUpdatedAt(lesson.getUpdatedAt());

        return dto;
    }
}