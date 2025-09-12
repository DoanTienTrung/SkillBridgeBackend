package com.skillbridge.skillbridgebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Web MVC để serve static files
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Cấu hình để serve uploaded files như static resources
     * Cho phép frontend truy cập được các file đã upload
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
        // Serve uploaded audio files
        // URL pattern: /api/uploads/audio/**
        // Physical location: uploads/audio/ (thư mục trong project root)
        registry.addResourceHandler("/uploads/audio/**")
                .addResourceLocations("file:uploads/audio/")
                .setCachePeriod(3600); // Cache 1 hour

        // Serve other uploaded files (for future use)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600); // Cache 1 hour
    }
}