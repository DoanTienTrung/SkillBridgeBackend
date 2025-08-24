package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.Service.FileStorageService;
import com.skillbridge.skillbridge_backend.Service.UserService;
import com.skillbridge.skillbridge_backend.dto.UserDto;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import com.skillbridge.skillbridge_backend.security.JwtHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
@Tag(name = "File Upload", description = "File upload endpoints for images and audio")
@Slf4j
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtHelper jwtHelper;

    /**
     * Upload avatar image
     */
    @PostMapping("/avatar")
    @Operation(summary = "Upload avatar image", description = "Upload user avatar image")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<UserDto>> uploadAvatar(
            Authentication authentication,
            @Parameter(description = "Image file", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading avatar for user: {}", authentication.getName());
            
            // Validate image file
            if (!fileStorageService.isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không hợp lệ. Chỉ chấp nhận file ảnh JPG, PNG, GIF với kích thước tối đa 10MB."));
            }
            
            // Save image file
            String imagePath = fileStorageService.saveImageFile(file);
            log.info("Image saved successfully at: {}", imagePath);
            
            // Update user avatar URL
            User currentUser = userService.findByEmail(authentication.getName());
            User updatedUser = userService.updateAvatar(currentUser.getId(), imagePath);
            
            // Create response
            UserDto userDto = new UserDto(updatedUser);
            Map<String, Object> response = new HashMap<>();
            response.put("user", userDto);
            response.put("avatarUrl", imagePath);
            
            return ResponseEntity.ok(ApiResponse.success("Upload ảnh đại diện thành công", userDto));
            
        } catch (Exception e) {
            log.error("Error uploading avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Upload ảnh đại diện thất bại", e.getMessage()));
        }
    }
    
    /**
     * Serve uploaded images
     */
    @GetMapping("/images/{filename:.+}")
    @Operation(summary = "Get uploaded image", description = "Serve uploaded image files")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "Image filename", required = true)
            @PathVariable String filename) {
        try {
            Path imagePath = Paths.get("uploads/images/" + filename);
            Resource resource = new UrlResource(imagePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = "image/jpeg"; // Default
                if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                }
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Error serving image: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
