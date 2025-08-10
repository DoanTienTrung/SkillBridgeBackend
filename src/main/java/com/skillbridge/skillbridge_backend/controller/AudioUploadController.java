package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.Service.FileStorageService;
import com.skillbridge.skillbridge_backend.dto.AudioUploadResponse;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xử lý upload và quản lý file audio
 */
@RestController
@RequestMapping("/audio")
@CrossOrigin(origins = "*")
@Tag(name = "Audio Upload", description = "Audio file upload and management endpoints")
public class AudioUploadController {

    private final FileStorageService fileStorageService;

    public AudioUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload file audio cho bài học listening
     * @param file File audio cần upload (MP3, WAV, M4A, AAC, OGG)
     * @return Response chứa thông tin file đã upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload audio file", 
        description = "Upload audio file for listening lessons. Supports MP3, WAV, M4A, AAC, OGG formats with max size 50MB"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "File uploaded successfully",
            content = @Content(schema = @Schema(implementation = AudioUploadResponse.class))
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
    public ResponseEntity<ApiResponse<AudioUploadResponse>> uploadAudio(
            @Parameter(description = "Audio file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Validate file trước khi xử lý
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
            }

            // Kiểm tra file có hợp lệ không
            if (!fileStorageService.isValidAudioFile(file)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không hợp lệ. Chỉ chấp nhận file audio MP3, WAV, M4A, AAC, OGG với kích thước tối đa 50MB"));
            }

            // Lưu file
            String savedFilePath = fileStorageService.saveAudioFile(file);

            // Tạo URL để truy cập file
            String audioUrl = "/api" + "/" + savedFilePath;

            // Lấy thông tin file
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String formattedSize = fileStorageService.formatFileSize(file.getSize());

            // Tạo response
            AudioUploadResponse response = new AudioUploadResponse();
            response.setAudioUrl(audioUrl);
            response.setOriginalFileName(file.getOriginalFilename());
            response.setSavedFileName(savedFilePath);
            response.setFileSizeBytes(file.getSize());
            response.setFormattedFileSize(formattedSize);
            response.setFileFormat(fileExtension);
            response.setMimeType(file.getContentType());
            response.setUploadTimestamp(System.currentTimeMillis());
            response.setMessage("Upload file audio thành công");

            return ResponseEntity.ok(
                ApiResponse.success("Upload file audio thành công", response)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi upload file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi upload file: " + e.getMessage()));
        }
    }

    /**
     * Xóa file audio đã upload
     * @param fileName Tên file cần xóa
     * @return Response xác nhận xóa file
     */
    @DeleteMapping("/{fileName}")
    @Operation(
        summary = "Delete audio file", 
        description = "Delete uploaded audio file by filename"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "File deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid filename or file not found"
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
    public ResponseEntity<ApiResponse<String>> deleteAudio(
            @Parameter(description = "Name of the file to delete", required = true)
            @PathVariable String fileName) {
        
        try {
            // Construct full path
            String fullPath = "uploads/audio/" + fileName;
            
            // Xóa file
            fileStorageService.deleteAudioFile(fullPath);

            return ResponseEntity.ok(
                ApiResponse.success("Xóa file audio thành công", "File " + fileName + " đã được xóa")
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi xóa file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi xóa file: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra file có tồn tại hay không
     * @param fileName Tên file cần kiểm tra
     * @return Response với thông tin file
     */
    @GetMapping("/check/{fileName}")
    @Operation(
        summary = "Check if audio file exists", 
        description = "Check if audio file exists and get file information"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FileStorageService.FileInfo>> checkAudioFile(
            @Parameter(description = "Name of the file to check", required = true)
            @PathVariable String fileName) {
        
        try {
            String fullPath = "uploads/audio/" + fileName;
            
            if (!fileStorageService.fileExists(fullPath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("File không tồn tại: " + fileName));
            }

            FileStorageService.FileInfo fileInfo = fileStorageService.getFileInfo(fullPath);
            
            return ResponseEntity.ok(
                ApiResponse.success("File tồn tại", fileInfo)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi kiểm tra file: " + e.getMessage()));
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
        description = "Validate audio file without uploading it"
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

            boolean isValid = fileStorageService.isValidAudioFile(file);
            
            if (isValid) {
                String message = String.format("File hợp lệ - %s (%s)", 
                    file.getOriginalFilename(), 
                    fileStorageService.formatFileSize(file.getSize()));
                
                return ResponseEntity.ok(ApiResponse.success(message, "valid"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không hợp lệ. Chỉ chấp nhận file audio MP3, WAV, M4A, AAC, OGG với kích thước tối đa 50MB"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi validate file: " + e.getMessage()));
        }
    }

    /**
     * Helper method để lấy extension của file
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}