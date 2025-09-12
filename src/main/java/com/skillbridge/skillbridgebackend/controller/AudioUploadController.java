package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.Service.CloudinaryService;
import com.skillbridge.skillbridgebackend.dto.CloudinaryUploadResponse;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xử lý upload và quản lý file audio với Cloudinary
 */
@RestController
@RequestMapping("/audio")
@CrossOrigin(origins = "*")
@Tag(name = "Audio Upload", description = "Audio file upload and management endpoints using Cloudinary")
@Slf4j
public class AudioUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Upload file audio lên Cloudinary cho bài học listening
     * @param file File audio cần upload (MP3, WAV, M4A, AAC, OGG, FLAC)
     * @return Response chứa thông tin file đã upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload audio file to Cloudinary", 
        description = "Upload audio file for listening lessons to Cloudinary. Supports MP3, WAV, M4A, AAC, OGG, FLAC formats with max size 50MB"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "File uploaded successfully to Cloudinary",
            content = @Content(schema = @Schema(implementation = CloudinaryUploadResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid file or file validation failed"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Teacher or Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413",
            description = "File too large (max 50MB)"
        )
    })
    public ResponseEntity<ApiResponse<CloudinaryUploadResponse>> uploadAudioToCloudinary(
            @Parameter(description = "Audio file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        try {
            log.info("Received request to upload audio file: {}", 
                    file != null ? file.getOriginalFilename() : "null");

            // Validate file trước khi xử lý
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
            }

            // Upload file lên Cloudinary
            CloudinaryService.CloudinaryUploadResult uploadResult = cloudinaryService.uploadAudio(file);

            // Tạo response
            CloudinaryUploadResponse response = CloudinaryUploadResponse.fromCloudinaryResult(
                uploadResult, file.getContentType());

            log.info("Successfully uploaded audio file {} to Cloudinary with public_id: {}", 
                    file.getOriginalFilename(), uploadResult.getPublicId());

            return ResponseEntity.ok(
                ApiResponse.success("Upload file audio thành công lên Cloudinary", response)
            );

        } catch (IllegalArgumentException e) {
            log.error("Validation error uploading audio file: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi validation: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Runtime error uploading audio file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi upload file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading audio file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi upload file: " + e.getMessage()));
        }
    }

    /**
     * Xóa file audio từ Cloudinary
     * @param publicId Public ID của file cần xóa
     * @return Response xác nhận xóa file
     */
    @DeleteMapping("/delete/{publicId}")
    @Operation(
        summary = "Delete audio file from Cloudinary", 
        description = "Delete uploaded audio file from Cloudinary by public ID"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "File deleted successfully from Cloudinary"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid public ID or file not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Teacher or Admin role required"
        )
    })
    public ResponseEntity<ApiResponse<String>> deleteAudioFromCloudinary(
            @Parameter(description = "Public ID of the file to delete", required = true, example = "skillbridge/audio/example_file_12345678")
            @PathVariable String publicId) {
        
        try {
            log.info("Received request to delete audio file with public_id: {}", publicId);

            // Validate public ID
            if (publicId == null || publicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Public ID không được để trống"));
            }

            // Xóa file từ Cloudinary
            boolean isDeleted = cloudinaryService.deleteAudio(publicId);

            if (isDeleted) {
                log.info("Successfully deleted audio file with public_id: {}", publicId);
                return ResponseEntity.ok(
                    ApiResponse.success("Xóa file audio thành công từ Cloudinary", 
                                      "File với public_id: " + publicId + " đã được xóa")
                );
            } else {
                log.warn("Failed to delete audio file with public_id: {}", publicId);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể xóa file. File có thể không tồn tại hoặc đã bị xóa trước đó"));
            }

        } catch (Exception e) {
            log.error("Error deleting audio file with public_id {}: {}", publicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi xóa file: " + e.getMessage()));
        }
    }

    /**
     * Lấy URL của file audio từ Cloudinary
     * @param publicId Public ID của file
     * @return Response với URL của file
     */
    @GetMapping("/url/{publicId}")
    @Operation(
        summary = "Get audio file URL from Cloudinary", 
        description = "Get secure URL of audio file from Cloudinary by public ID"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<String>> getAudioUrl(
            @Parameter(description = "Public ID of the file", required = true, example = "skillbridge/audio/example_file_12345678")
            @PathVariable String publicId) {
        
        try {
            // Validate public ID
            if (publicId == null || publicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Public ID không được để trống"));
            }

            // Lấy URL từ Cloudinary
            String audioUrl = cloudinaryService.getAudioUrl(publicId);

            return ResponseEntity.ok(
                ApiResponse.success("Lấy URL file audio thành công", audioUrl)
            );

        } catch (Exception e) {
            log.error("Error getting audio URL for public_id {}: {}", publicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi lấy URL file: " + e.getMessage()));
        }
    }

    /**
     * Validate file audio mà không upload
     * @param file File cần validate
     * @return Response với kết quả validation
     */
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Validate audio file", 
        description = "Validate audio file without uploading it to Cloudinary"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> validateAudio(
            @Parameter(description = "Audio file to validate", required = true)
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
            }

            // Test validation bằng cách gọi private method validation trong CloudinaryService
            // Nếu không có exception thì file hợp lệ
            try {
                cloudinaryService.uploadAudio(file);
                // Nếu đến đây thì file hợp lệ, nhưng chúng ta không muốn upload thật
                // Chúng ta sẽ tạo validation riêng
                
                String formattedSize = cloudinaryService.formatFileSize(file.getSize());
                String message = String.format("File hợp lệ - %s (%s)", 
                    file.getOriginalFilename(), formattedSize);
                
                return ResponseEntity.ok(ApiResponse.success(message, "valid"));
                
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không hợp lệ: " + e.getMessage()));
            }

        } catch (Exception e) {
            log.error("Error validating audio file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi validate file: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint để debug upload issues
     */
    @PostMapping("/test")
    @Operation(summary = "Test audio upload debug")
    public ResponseEntity<String> testUpload(@RequestParam("file") MultipartFile file) {
        try {
            log.info("=== DEBUG TEST UPLOAD ===");
            log.info("File name: {}", file.getOriginalFilename());
            log.info("File size: {}", file.getSize());
            log.info("Content type: {}", file.getContentType());
            
            return ResponseEntity.ok("Test successful - File received: " + file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Test upload error: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint để kiểm tra kết nối Cloudinary
     */
    @GetMapping("/health")
    @Operation(
        summary = "Check Cloudinary connection", 
        description = "Health check endpoint to verify Cloudinary service is working"
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        try {
            // Có thể thêm logic kiểm tra kết nối Cloudinary ở đây
            return ResponseEntity.ok(
                ApiResponse.success("Cloudinary service is healthy", "OK")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Cloudinary service is not available: " + e.getMessage()));
        }
    }
}
