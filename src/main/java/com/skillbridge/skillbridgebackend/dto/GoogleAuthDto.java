package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthDto {
    private String accessToken;
    private String idToken;
    private String code;
}