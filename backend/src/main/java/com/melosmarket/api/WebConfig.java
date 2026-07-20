package com.melosmarket.api;

import java.nio.file.Path;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path workerReferencesDir;
    private final Path workerProfileImagesDir;
    private final Path problemImagesDir;
    private final String[] allowedOrigins;

    public WebConfig(
            @Value("${melosmarket.uploads.worker-references-dir}") String workerReferencesDir,
            @Value("${melosmarket.uploads.worker-profile-images-dir}") String workerProfileImagesDir,
            @Value("${melosmarket.uploads.problem-images-dir}") String problemImagesDir,
            @Value("${melosmarket.cors.allowed-origins}") String allowedOrigins) {
        this.workerReferencesDir = Path.of(workerReferencesDir).toAbsolutePath().normalize();
        this.workerProfileImagesDir = Path.of(workerProfileImagesDir).toAbsolutePath().normalize();
        this.problemImagesDir = Path.of(problemImagesDir).toAbsolutePath().normalize();
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/worker-references/**")
                .addResourceLocations(workerReferencesDir.toUri() + "/");
        registry.addResourceHandler("/uploads/worker-profile-images/**")
                .addResourceLocations(workerProfileImagesDir.toUri() + "/");
        registry.addResourceHandler("/uploads/problem-images/**")
                .addResourceLocations(problemImagesDir.toUri() + "/");
    }
}
