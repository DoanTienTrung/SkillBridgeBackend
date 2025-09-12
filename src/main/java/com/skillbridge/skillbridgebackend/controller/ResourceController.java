package com.skillbridge.skillbridgebackend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/uploads")  // ← ĐÃ SỬA: từ /resources thành /uploads
@CrossOrigin(origins = "*")
public class ResourceController {

    // Map để xác định loại file
    private static final Map<String, MediaType> MEDIA_TYPE_MAP = new HashMap<>();

    static {
        MEDIA_TYPE_MAP.put("mp3", MediaType.parseMediaType("audio/mpeg"));
        MEDIA_TYPE_MAP.put("wav", MediaType.parseMediaType("audio/wav"));
        MEDIA_TYPE_MAP.put("m4a", MediaType.parseMediaType("audio/mp4"));
        MEDIA_TYPE_MAP.put("aac", MediaType.parseMediaType("audio/aac"));
        MEDIA_TYPE_MAP.put("ogg", MediaType.parseMediaType("audio/ogg"));
    }

    /**
     * Serve audio files
     * URL: GET /api/uploads/audio/{filename}
     */
    @GetMapping("/audio/{filename}")
    public ResponseEntity<Resource> getAudio(@PathVariable String filename) {
        try {
            // Tìm file trong thư mục uploads/audio/
            Path filePath = Paths.get("uploads/audio/", filename).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Xác định loại file
                MediaType mediaType = determineMediaType(filename);

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test endpoint để check hệ thống
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "File serving is working");
        health.put("workingDirectory", System.getProperty("user.dir"));

        // Check thư mục uploads/audio có tồn tại không
        boolean audioDirExists = Paths.get("uploads/audio/").toFile().exists();
        health.put("audioDirectoryExists", audioDirExists);

        return ResponseEntity.ok(health);
    }

    /**
     * Xác định loại file dựa trên đuôi file
     */
    private MediaType determineMediaType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return MEDIA_TYPE_MAP.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM);
    }
}