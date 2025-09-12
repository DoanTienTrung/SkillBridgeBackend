package com.skillbridge.skillbridgebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserProfileUpdateDto {
    
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    private String school;
    
    private String major;
    
    private String academicYear;
} 