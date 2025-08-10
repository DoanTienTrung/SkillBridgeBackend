package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.dto.*;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.Service.UserService;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // TODO: Add JwtUtil when implementing JWT
    // @Autowired
    // private JwtUtil jwtUtil;

    /**
     * Đăng ký user mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody UserRegistrationDto request) {
        try {
            User user = userService.registerUser(request);
            UserDto userDto = new UserDto(user);

            ApiResponse<UserDto> response = ApiResponse.success("Đăng ký thành công", userDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            ApiResponse<UserDto> response = ApiResponse.error("Đăng ký thất bại", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Đăng nhập
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user info
            User user = userService.findByEmail(request.getEmail());
            UserDto userDto = new UserDto(user);

            // TODO: Generate JWT token when implementing JWT
            String token = "temporary-token"; // Placeholder

            // Create response
            LoginResponse loginResponse = new LoginResponse(token, userDto);
            ApiResponse<LoginResponse> response = ApiResponse.success("Đăng nhập thành công", loginResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<LoginResponse> response = ApiResponse.error("Email hoặc mật khẩu không đúng");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Lấy thông tin user hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                ApiResponse<UserDto> response = ApiResponse.error("Chưa đăng nhập");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);
            UserDto userDto = new UserDto(user);

            ApiResponse<UserDto> response = ApiResponse.success(userDto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<UserDto> response = ApiResponse.error("Không thể lấy thông tin người dùng");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Đăng xuất (placeholder)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        SecurityContextHolder.clearContext();
        ApiResponse<String> response = ApiResponse.success("Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint để kiểm tra API hoạt động
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("API đang hoạt động tốt!", "Hello from SkillBridge API"));
    }
}