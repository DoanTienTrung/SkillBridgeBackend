package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.Service.VocabularyService;
import com.skillbridge.skillbridgebackend.dto.VocabularyCreateDto;
import com.skillbridge.skillbridgebackend.dto.LessonVocabularyDto;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/listening-lessons")
@CrossOrigin(origins = "*")
@Tag(name = "Vocabulary Management", description = "Lesson vocabulary management endpoints")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @PostMapping("/{lessonId}/vocabularies")
    @Operation(summary = "Add vocabulary to lesson")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LessonVocabularyDto>> addVocabularyToLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody VocabularyCreateDto createDto) {

        LessonVocabularyDto result = vocabularyService.addVocabularyToLesson(lessonId, createDto);
        return ResponseEntity.ok(ApiResponse.success("Thêm từ vựng thành công", result));
    }

    @GetMapping("/{lessonId}/vocabularies")
    @Operation(summary = "Get lesson vocabularies")
    public ResponseEntity<ApiResponse<List<LessonVocabularyDto>>> getLessonVocabularies(
            @PathVariable Long lessonId) {

        List<LessonVocabularyDto> vocabularies = vocabularyService.getLessonVocabularies(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách từ vựng thành công", vocabularies));
    }

    @DeleteMapping("/{lessonId}/vocabularies/{vocabularyId}")
    @Operation(summary = "Remove vocabulary from lesson")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> removeVocabularyFromLesson(
            @PathVariable Long lessonId,
            @PathVariable Long vocabularyId) {

        vocabularyService.removeVocabularyFromLesson(lessonId, vocabularyId);
        return ResponseEntity.ok(ApiResponse.success("Xóa từ vựng thành công", null));
    }
}