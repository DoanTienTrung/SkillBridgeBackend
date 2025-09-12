package com.skillbridge.skillbridgebackend.mapper;

import com.skillbridge.skillbridgebackend.dto.ListeningLessonDto;
import com.skillbridge.skillbridgebackend.dto.ListeningLessonCreateDto;
import com.skillbridge.skillbridgebackend.entity.ListeningLesson;
import com.skillbridge.skillbridgebackend.entity.Category;
import com.skillbridge.skillbridgebackend.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class để convert giữa ListeningLesson Entity và DTO
 */
@Component
public class ListeningLessonMapper {

    /**
     * Convert từ Entity sang DTO
     * @param lesson ListeningLesson entity
     * @return ListeningLessonDto
     */
    public static ListeningLessonDto toDto(ListeningLesson lesson) {
        if (lesson == null) {
            return null;
        }

        ListeningLessonDto dto = new ListeningLessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setDescription(lesson.getDescription());
        dto.setLevel(lesson.getLevel().name());
        dto.setStatus(lesson.getStatus().name());
        dto.setAudioUrl(lesson.getAudioUrl());
        dto.setTranscript(lesson.getTranscript());
        dto.setDurationSeconds(lesson.getDurationSeconds());
        dto.setCreatedAt(lesson.getCreatedAt());
        dto.setUpdatedAt(lesson.getUpdatedAt());

        // Map category information
        if (lesson.getCategory() != null) {
            dto.setCategoryId(lesson.getCategory().getId());
            dto.setCategoryName(lesson.getCategory().getName());
        }

        // Map creator information
        if (lesson.getCreatedBy() != null) {
            dto.setCreatedById(lesson.getCreatedBy().getId());
            dto.setCreatedByName(lesson.getCreatedBy().getFullName());
        }

        // Map questions count
        if (lesson.getQuestions() != null) {
            dto.setQuestionCount(lesson.getQuestions().size());
        }

        return dto;
    }

    /**
     * Convert từ CreateDto sang Entity (cho tạo mới)
     * @param createDto ListeningLessonCreateDto
     * @param category Category entity
     * @param createdBy User entity (teacher)
     * @return ListeningLesson entity
     */
    public static ListeningLesson toEntity(ListeningLessonCreateDto createDto,
                                           Category category,
                                           User createdBy) {
        if (createDto == null) {
            return null;
        }

        ListeningLesson lesson = new ListeningLesson();
        lesson.setTitle(createDto.getTitle());
        lesson.setDescription(createDto.getDescription());
        lesson.setLevel(createDto.getLevel());
        lesson.setAudioUrl(createDto.getAudioUrl());
        lesson.setTranscript(createDto.getTranscript());
        lesson.setDurationSeconds(createDto.getDurationSeconds());
        lesson.setCategory(category);
        lesson.setCreatedBy(createdBy);
        lesson.setStatus(ListeningLesson.Status.DRAFT); // Default status

        return lesson;
    }

    /**
     * Update entity từ DTO (cho cập nhật)
     * @param lesson Entity cần update
     * @param updateDto DTO chứa data mới
     * @param category Category entity (nếu có thay đổi)
     */
    public static void updateEntityFromDto(ListeningLesson lesson,
                                           ListeningLessonCreateDto updateDto,
                                           Category category) {
        if (lesson == null || updateDto == null) {
            return;
        }

        lesson.setTitle(updateDto.getTitle());
        lesson.setDescription(updateDto.getDescription());
        lesson.setLevel(updateDto.getLevel());
        lesson.setAudioUrl(updateDto.getAudioUrl());
        lesson.setTranscript(updateDto.getTranscript());
        lesson.setDurationSeconds(updateDto.getDurationSeconds());

        if (category != null) {
            lesson.setCategory(category);
        }
    }

    /**
     * Convert list Entity sang list DTO
     * @param lessons List của ListeningLesson entities
     * @return List của ListeningLessonDto
     */
    public static List<ListeningLessonDto> toDtoList(List<ListeningLesson> lessons) {
        if (lessons == null) {
            return null;
        }

        return lessons.stream()
                .map(ListeningLessonMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert sang DTO với thông tin tóm tắt (cho danh sách)
     * @param lesson ListeningLesson entity
     * @return ListeningLessonDto với thông tin cơ bản
     */
    public static ListeningLessonDto toSummaryDto(ListeningLesson lesson) {
        if (lesson == null) {
            return null;
        }

        ListeningLessonDto dto = new ListeningLessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setDescription(lesson.getDescription());
        dto.setLevel(lesson.getLevel().name());
        dto.setStatus(lesson.getStatus().name());
        dto.setDurationSeconds(lesson.getDurationSeconds());
        dto.setCreatedAt(lesson.getCreatedAt());

        // Chỉ lấy thông tin cơ bản của category
        if (lesson.getCategory() != null) {
            dto.setCategoryId(lesson.getCategory().getId());
            dto.setCategoryName(lesson.getCategory().getName());
        }

        // Chỉ lấy tên teacher
        if (lesson.getCreatedBy() != null) {
            dto.setCreatedByName(lesson.getCreatedBy().getFullName());
        }

        // Không load transcript và audio URL cho danh sách (tối ưu performance)
        dto.setTranscript(null);
        dto.setAudioUrl(null);

        return dto;
    }

    /**
     * Convert sang DTO với đầy đủ thông tin (cho chi tiết)
     * @param lesson ListeningLesson entity
     * @return ListeningLessonDto với đầy đủ thông tin
     */
    public static ListeningLessonDto toDetailDto(ListeningLesson lesson) {
        if (lesson == null) {
            return null;
        }

        // Sử dụng method toDto() cơ bản và thêm thông tin chi tiết
        ListeningLessonDto dto = toDto(lesson);

        // Thêm thông tin về số lượng câu hỏi, progress, etc.
        if (lesson.getQuestions() != null) {
            dto.setQuestionCount(lesson.getQuestions().size());
        }

        return dto;
    }

    /**
     * Copy các field có thể update từ DTO sang Entity
     * @param source DTO nguồn
     * @param target Entity đích
     */
    public static void copyUpdatableFields(ListeningLessonCreateDto source,
                                           ListeningLesson target) {
        if (source == null || target == null) {
            return;
        }

        if (source.getTitle() != null) {
            target.setTitle(source.getTitle());
        }

        if (source.getDescription() != null) {
            target.setDescription(source.getDescription());
        }

        if (source.getLevel() != null) {
            target.setLevel(source.getLevel());
        }

        if (source.getAudioUrl() != null) {
            target.setAudioUrl(source.getAudioUrl());
        }

        if (source.getTranscript() != null) {
            target.setTranscript(source.getTranscript());
        }

        if (source.getDurationSeconds() != null) {
            target.setDurationSeconds(source.getDurationSeconds());
        }
    }
}