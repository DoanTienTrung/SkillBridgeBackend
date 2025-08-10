package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.dto.UserDto;
import com.skillbridge.skillbridge_backend.dto.UserRegistrationDto;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.Service.UserService;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Lấy thông tin profile của user hiện tại
     */
    @GetMapping("/profile")
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
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            Authentication authentication,
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserActive(@PathVariable Long id) {
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