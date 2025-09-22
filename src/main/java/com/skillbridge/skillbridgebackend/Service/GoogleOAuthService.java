package com.skillbridge.skillbridgebackend.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.skillbridge.skillbridgebackend.dto.GoogleAuthDto;
import com.skillbridge.skillbridgebackend.dto.GoogleUserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

@Service
@Slf4j
public class GoogleOAuthService {

    @Value("${google.oauth2.client-id}")
    private String clientId;

    @Value("${google.oauth2.client-secret}")
    private String clientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String redirectUri;

    private GoogleAuthorizationCodeFlow flow;
    private GoogleIdTokenVerifier verifier;
    private final WebClient webClient;

    public GoogleOAuthService() {
        this.webClient = WebClient.builder().build();
    }

    @PostConstruct
    public void init() {
        try {
            // Setup Google OAuth Flow
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
            web.setClientId(clientId);
            web.setClientSecret(clientSecret);

            GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
            clientSecrets.setWeb(web);

            flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    Arrays.asList("email", "profile")
            ).build();

            // Setup ID Token Verifier
            verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            log.info("Google OAuth service initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize Google OAuth service", e);
            throw new RuntimeException("Google OAuth initialization failed", e);
        }
    }

    /**
     * Tạo URL authorize cho Google OAuth
     */
    public String getAuthorizationUrl() {
        try {
            return flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setState("state-" + System.currentTimeMillis()) // CSRF protection
                    .build();
        } catch (Exception e) {
            log.error("Error creating authorization URL", e);
            throw new RuntimeException("Failed to create Google authorization URL", e);
        }
    }

    /**
     * Xử lý authorization code và lấy user profile
     */
    public GoogleUserProfileDto processAuthorizationCode(String code) {
        try {
            log.info("Processing Google authorization code");

            // Exchange code for tokens
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            // Verify and decode ID token
            GoogleIdToken idToken = verifier.verify(tokenResponse.getIdToken());
            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            // Create user profile DTO
            GoogleUserProfileDto profile = new GoogleUserProfileDto();
            profile.setId(payload.getSubject());
            profile.setEmail(payload.getEmail());
            profile.setName((String) payload.get("name"));
            profile.setPicture((String) payload.get("picture"));
            profile.setEmailVerified(payload.getEmailVerified());
            profile.setLocale((String) payload.get("locale"));

            log.info("Google user profile retrieved successfully for: {}", profile.getEmail());
            return profile;

        } catch (Exception e) {
            log.error("Error processing Google authorization code", e);
            throw new RuntimeException("Failed to process Google authorization", e);
        }
    }

    /**
     * Verify Google ID Token trực tiếp (cho frontend gửi token)
     */
    public GoogleUserProfileDto verifyIdToken(String idTokenString) {
        try {
            log.info("Verifying Google ID token");

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            GoogleUserProfileDto profile = new GoogleUserProfileDto();
            profile.setId(payload.getSubject());
            profile.setEmail(payload.getEmail());
            profile.setName((String) payload.get("name"));
            profile.setPicture((String) payload.get("picture"));
            profile.setEmailVerified(payload.getEmailVerified());

            log.info("Google ID token verified successfully for: {}", profile.getEmail());
            return profile;

        } catch (Exception e) {
            log.error("Error verifying Google ID token", e);
            throw new RuntimeException("Invalid Google ID token", e);
        }
    }
}