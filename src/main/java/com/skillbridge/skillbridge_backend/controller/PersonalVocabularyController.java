package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.Service.VocabularyService;
import com.skillbridge.skillbridge_backend.dto.UserVocabularyDto;
import com.skillbridge.skillbridge_backend.dto.PersonalVocabularyCreateDto;
import com.skillbridge.skillbridge_backend.entity.UserVocabulary;
import com.skillbridge.skillbridge_backend.entity.Vocabulary;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import com.skillbridge.skillbridge_backend.security.JwtHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vocabulary")
@CrossOrigin(origins = "*")
@Tag(name = "Personal Vocabulary", description = "Personal vocabulary management endpoints")
@Slf4j
public class PersonalVocabularyController {

    private final VocabularyService vocabularyService;
    private final JwtHelper jwtHelper;

    public PersonalVocabularyController(VocabularyService vocabularyService, JwtHelper jwtHelper) {
        this.vocabularyService = vocabularyService;
        this.jwtHelper = jwtHelper;
    }

    /**
     * Save word to personal vocabulary
     */
    @PostMapping("/save")
    @Operation(summary = "Save word to personal vocabulary", description = "Save a word to user's personal vocabulary list")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<UserVocabularyDto>> saveWord(
            @Parameter(description = "Word data", required = true)
            @RequestBody PersonalVocabularyCreateDto vocabularyDto) {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
            }

            if (vocabularyDto.getWord() == null || vocabularyDto.getWord().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Từ vựng không được để trống"));
            }

            UserVocabulary savedVocab = vocabularyService.saveToPersonalVocabulary(
                currentUser.getId(), vocabularyDto);
            
            UserVocabularyDto dto = new UserVocabularyDto(savedVocab);
            return ResponseEntity.ok(ApiResponse.success("Lưu từ vựng thành công", dto));
        } catch (Exception e) {
            log.error("Error saving vocabulary: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lưu từ vựng thất bại", e.getMessage()));
        }
    }

    /**
     * Get user's personal vocabulary
     */
    @GetMapping("/my-vocabulary")
    @Operation(summary = "Get personal vocabulary", description = "Get user's personal vocabulary list")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<UserVocabularyDto>>> getMyVocabulary() {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
            }

            List<UserVocabulary> vocabulary = vocabularyService.getUserVocabulary(currentUser.getId());
            List<UserVocabularyDto> vocabularyDtos = vocabulary.stream()
                .map(UserVocabularyDto::new)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách từ vựng thành công", vocabularyDtos));
        } catch (Exception e) {
            log.error("Error getting vocabulary: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lấy danh sách từ vựng thất bại", e.getMessage()));
        }
    }

    /**
     * Update vocabulary status
     */
    @PutMapping("/{vocabularyId}/status")
    @Operation(summary = "Update vocabulary status", description = "Update vocabulary learning status")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<UserVocabularyDto>> updateVocabularyStatus(
            @Parameter(description = "Vocabulary ID", required = true)
            @PathVariable Long vocabularyId,
            @Parameter(description = "New status", required = true)
            @RequestParam UserVocabulary.Status status) {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
            }

            UserVocabulary updated = vocabularyService.updateVocabularyStatus(
                currentUser.getId(), vocabularyId, status);
            
            UserVocabularyDto dto = new UserVocabularyDto(updated);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", dto));
        } catch (Exception e) {
            log.error("Error updating vocabulary status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Cập nhật trạng thái thất bại", e.getMessage()));
        }
    }

    /**
     * Remove word from personal vocabulary
     */
    @DeleteMapping("/{vocabularyId}")
    @Operation(summary = "Remove word from personal vocabulary", description = "Remove a word from user's personal vocabulary")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<String>> removeWord(
            @Parameter(description = "Vocabulary ID", required = true)
            @PathVariable Long vocabularyId) {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
            }

            vocabularyService.removeFromPersonalVocabulary(currentUser.getId(), vocabularyId);
            return ResponseEntity.ok(ApiResponse.success("Xóa từ vựng thành công", null));
        } catch (Exception e) {
            log.error("Error removing vocabulary: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Xóa từ vựng thất bại", e.getMessage()));
        }
    }

    /**
     * Look up word meaning
     */
    @GetMapping("/lookup/{word}")
    @Operation(summary = "Look up word", description = "Look up word meaning in dictionary")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<Vocabulary>> lookupWord(
            @Parameter(description = "Word to look up", required = true)
            @PathVariable String word) {
        try {
            Vocabulary vocabulary = vocabularyService.lookupWord(word);
            
            if (vocabulary != null) {
                return ResponseEntity.ok(ApiResponse.success("Tìm thấy từ vựng", vocabulary));
            } else {
                // Return empty response, frontend will handle external dictionary
                Map<String, String> suggestion = new HashMap<>();
                suggestion.put("word", word);
                suggestion.put("message", "Từ vựng chưa có trong hệ thống, hãy thêm nghĩa");
                
                return ResponseEntity.ok(ApiResponse.success("Từ vựng chưa có trong hệ thống", null));
            }
        } catch (Exception e) {
            log.error("Error looking up word: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Tra từ thất bại", e.getMessage()));
        }
    }

    /**
     * Get vocabulary statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get vocabulary statistics", description = "Get user's vocabulary learning statistics")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVocabularyStats() {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
            }

            List<UserVocabulary> allVocabulary = vocabularyService.getUserVocabulary(currentUser.getId());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", allVocabulary.size());
            stats.put("learning", allVocabulary.stream().filter(v -> v.getStatus() == UserVocabulary.Status.LEARNING).count());
            stats.put("mastered", allVocabulary.stream().filter(v -> v.getStatus() == UserVocabulary.Status.MASTERED).count());
            stats.put("difficult", allVocabulary.stream().filter(v -> v.getStatus() == UserVocabulary.Status.DIFFICULT).count());

            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê từ vựng thành công", stats));
        } catch (Exception e) {
            log.error("Error getting vocabulary stats: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lấy thống kê từ vựng thất bại", e.getMessage()));
        }
    }
}
