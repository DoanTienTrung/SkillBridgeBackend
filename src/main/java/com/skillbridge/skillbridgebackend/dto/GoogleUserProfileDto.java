package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserProfileDto {
    private String id;
    private String email;
    private String name;
    private String picture;
    private Boolean emailVerified;
    private String locale;
}