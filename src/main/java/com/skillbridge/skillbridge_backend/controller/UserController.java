package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.dto.UserDto;
import com.skillbridge.skillbridge_backend.dto.UserRegistrationDto;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.Service.UserService;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "User profile and management endpoints")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Lấy thông tin profile của user hiện tại
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            UserDto userDto = new UserDto(user);

            return ResponseEntity.ok(ApiResponse.success(userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể lấy thông tin profile"));
        }
    }

    /**
     * Cập nhật profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update current user's profile information")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            Authentication authentication,
            @Parameter(description = "Updated profile data", required = true)
            @Valid @RequestBody UserRegistrationDto updateDto) {

        try {
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email);

            User updatedUser = userService.updateUser(currentUser.getId(), updateDto);
            UserDto userDto = new UserDto(updatedUser);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật profile thành công", userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể cập nhật profile", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả học viên (chỉ cho teacher/admin)
     */
    @GetMapping("/students")
    @Operation(summary = "Get all students", description = "Get list of all students (Teacher/Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllStudents() {
        try {
            List<User> students = userService.getAllStudents();
            List<UserDto> studentDtos = students.stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học viên thành công", studentDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể lấy danh sách học viên"));
        }
    }

    /**
     * Lấy thông tin user theo ID (chỉ cho admin)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user information by ID (Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        try {
            User user = userService.findById(id);
            UserDto userDto = new UserDto(user);

            return ResponseEntity.ok(ApiResponse.success(userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không tìm thấy người dùng"));
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa user (chỉ cho admin)
     */
    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle user active status", description = "Activate/Deactivate user account (Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserActive(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        try {
            User user = userService.toggleUserActive(id);
            UserDto userDto = new UserDto(user);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể cập nhật trạng thái user"));
        }
    }
}