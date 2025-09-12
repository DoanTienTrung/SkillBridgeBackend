package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.dto.*;
import com.skillbridge.skillbridgebackend.entity.User;
import com.skillbridge.skillbridgebackend.Service.UserService;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import com.skillbridge.skillbridgebackend.security.JwtUtil;
import com.skillbridge.skillbridgebackend.security.JwtHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtHelper jwtHelper;

    /**
     * Đăng ký user mới
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new user", 
        description = "Register a new student account with email, password and personal information"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid input or email already exists",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Parameter(description = "User registration data", required = true)
            @Valid @RequestBody UserRegistrationDto request) {
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
    @Operation(
        summary = "User login", 
        description = "Authenticate user with email and password, returns JWT token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or email already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequest request) {
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

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

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
    @Operation(
        summary = "Get current user profile", 
        description = "Get authenticated user's profile information"
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Not authenticated",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        try {
            User user = jwtHelper.getCurrentUser();
            if (user == null) {
                ApiResponse<UserDto> response = ApiResponse.error("Chưa đăng nhập");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

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
    @Operation(summary = "User logout", description = "Logout current user session")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<String>> logout() {
        SecurityContextHolder.clearContext();
        ApiResponse<String> response = ApiResponse.success("Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint để kiểm tra API hoạt động
     */
    @GetMapping("/test")
    @Operation(summary = "Test API", description = "Test endpoint to verify API is working")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API is working")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("API đang hoạt động tốt!", "Hello from SkillBridge API"));
    }

    /**
     * Validate and get JWT token info
     */
    @GetMapping("/token-info")
    @Operation(summary = "Get JWT token info", description = "Decode and validate JWT token information")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTokenInfo(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.getEmailFromToken(token);

            if (!jwtUtil.validateToken(token, email)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid or expired token"));
            }

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("email", email);
            tokenInfo.put("userId", jwtUtil.getUserIdFromToken(token));
            tokenInfo.put("role", jwtUtil.getRoleFromToken(token));
            tokenInfo.put("fullName", jwtUtil.getFullNameFromToken(token));
            tokenInfo.put("isActive", jwtUtil.getIsActiveFromToken(token));
            tokenInfo.put("issuedAt", jwtUtil.getIssuedAtDateFromToken(token));
            tokenInfo.put("expiresAt", jwtUtil.getExpirationDateFromToken(token));

            return ResponseEntity.ok(ApiResponse.success("Token info retrieved successfully", tokenInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error processing token", e.getMessage()));
        }
    }
}
