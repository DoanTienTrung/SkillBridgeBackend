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
 * Controller xử lý upload và quản lý file ảnh với Cloudinary
 */
@RestController
@RequestMapping("/image")
@CrossOrigin(origins = "*")
@Tag(name = "Image Upload", description = "Image file upload and management endpoints using Cloudinary")
@Slf4j
public class ImageUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Upload file ảnh lên Cloudinary
     * @param file File ảnh cần upload (JPG, PNG, GIF, BMP, WEBP)
     * @return Response chứa thông tin file đã upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload image file to Cloudinary", 
        description = "Upload image file to Cloudinary. Supports JPG, PNG, GIF, BMP, WEBP formats with max size 10MB"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('STUDENT')")
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
        )
    })
    public ResponseEntity<ApiResponse<CloudinaryUploadResponse>> uploadImageToCloudinary(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        try {
            log.info("Received request to upload image file: {}", 
                    file != null ? file.getOriginalFilename() : "null");

            // Validate file trước khi xử lý
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
            }

            // Upload file lên Cloudinary
            CloudinaryService.CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(file);

            // Tạo response
            CloudinaryUploadResponse response = CloudinaryUploadResponse.fromCloudinaryResult(
                uploadResult, file.getContentType());

            log.info("Successfully uploaded image file {} to Cloudinary with public_id: {}", 
                    file.getOriginalFilename(), uploadResult.getPublicId());

            return ResponseEntity.ok(
                ApiResponse.success("Upload file ảnh thành công lên Cloudinary", response)
            );

        } catch (IllegalArgumentException e) {
            log.error("Validation error uploading image file: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi validation: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Runtime error uploading image file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi upload file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading image file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi upload file: " + e.getMessage()));
        }
    }

    /**
     * Xóa file ảnh từ Cloudinary
     * @param publicId Public ID của file cần xóa
     * @return Response xác nhận xóa file
     */
    @DeleteMapping("/delete/{publicId}")
    @Operation(
        summary = "Delete image file from Cloudinary", 
        description = "Delete uploaded image file from Cloudinary by public ID"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteImageFromCloudinary(
            @Parameter(description = "Public ID of the file to delete", required = true)
            @PathVariable String publicId) {
        
        try {
            log.info("Received request to delete image file with public_id: {}", publicId);

            if (publicId == null || publicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Public ID không được để trống"));
            }

            boolean isDeleted = cloudinaryService.deleteImage(publicId);

            if (isDeleted) {
                return ResponseEntity.ok(
                    ApiResponse.success("Xóa file ảnh thành công từ Cloudinary", 
                                      "File với public_id: " + publicId + " đã được xóa")
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể xóa file. File có thể không tồn tại"));
            }

        } catch (Exception e) {
            log.error("Error deleting image file with public_id {}: {}", publicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi xóa file: " + e.getMessage()));
        }
    }

    /**
     * Lấy URL của file ảnh từ Cloudinary
     * @param publicId Public ID của file
     * @param width Chiều rộng mong muốn (optional)
     * @param height Chiều cao mong muốn (optional)
     * @return Response với URL của file
     */
    @GetMapping("/url/{publicId}")
    @Operation(
        summary = "Get image file URL from Cloudinary", 
        description = "Get secure URL of image file from Cloudinary by public ID with optional resizing"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<String>> getImageUrl(
            @Parameter(description = "Public ID of the file", required = true)
            @PathVariable String publicId,
            @Parameter(description = "Image width for resizing")
            @RequestParam(required = false) Integer width,
            @Parameter(description = "Image height for resizing")
            @RequestParam(required = false) Integer height) {
        
        try {
            if (publicId == null || publicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Public ID không được để trống"));
            }

            String imageUrl = cloudinaryService.getImageUrl(publicId, width, height);

            return ResponseEntity.ok(
                ApiResponse.success("Lấy URL file ảnh thành công", imageUrl)
            );

        } catch (Exception e) {
            log.error("Error getting image URL for public_id {}: {}", publicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống khi lấy URL file: " + e.getMessage()));
        }
    }
}
