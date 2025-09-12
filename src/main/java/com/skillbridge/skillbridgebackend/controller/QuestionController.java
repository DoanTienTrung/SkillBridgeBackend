package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.Service.QuestionService;
import com.skillbridge.skillbridgebackend.dto.QuestionCreateDto;
import com.skillbridge.skillbridgebackend.dto.QuestionDto;
import com.skillbridge.skillbridgebackend.dto.QuestionUpdateDto;
import com.skillbridge.skillbridgebackend.entity.Question;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@CrossOrigin(origins = "*")
@Tag(name = "Questions", description = "Question management endpoints")
public class QuestionController {
    
    private final QuestionService questionService;
    
    // Constructor injection
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get questions by lesson", description = "Get all questions for a specific lesson")
    public ResponseEntity<ApiResponse<List<QuestionDto>>> getQuestionsByLesson(
            @PathVariable Long lessonId,
            @RequestParam Question.LessonType lessonType) {
        try {
            List<QuestionDto> questions = questionService.getQuestionsByLesson(lessonId, lessonType);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách câu hỏi thành công", questions));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách câu hỏi: " + e.getMessage()));
        }
    }
    
    @PostMapping
    @Operation(summary = "Create new question", description = "Create a new question for a lesson")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionDto>> createQuestion(
            @Valid @RequestBody QuestionCreateDto dto) {
        try {
            QuestionDto question = questionService.createQuestion(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo câu hỏi thành công", question));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi tạo câu hỏi: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update question", description = "Update an existing question")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionUpdateDto dto) {
        try {
            QuestionDto question = questionService.updateQuestion(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật câu hỏi thành công", question));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi cập nhật câu hỏi: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete question", description = "Delete a question")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa câu hỏi thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xóa câu hỏi: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID", description = "Get a specific question by ID")
    public ResponseEntity<ApiResponse<QuestionDto>> getQuestionById(@PathVariable Long id) {
        try {
            QuestionDto question = questionService.getQuestionById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin câu hỏi thành công", question));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy thông tin câu hỏi: " + e.getMessage()));
        }
    }
}