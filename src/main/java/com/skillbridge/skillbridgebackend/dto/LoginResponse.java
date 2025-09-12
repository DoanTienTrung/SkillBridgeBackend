package com.skillbridge.skillbridgebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private UserDto user;
    private Long expiresIn; // Token expiration time in seconds

    // Constructor without tokenType and expiresIn
    public LoginResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
        this.tokenType = "Bearer";
        this.expiresIn = 86400L; // 24 hours in seconds
    }

    // Constructor with all fields except tokenType
    public LoginResponse(String token, UserDto user, Long expiresIn) {
        this.token = token;
        this.user = user;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }
}