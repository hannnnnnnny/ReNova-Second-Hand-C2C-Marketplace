package com.novacart.store.config;

import com.novacart.store.service.ImageUploadService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Exposes the uploaded-image directory at the configured public URL path
 * via Spring's resource handler — files are served straight off disk, no
 * controller hop. The path is normalized in ImageUploadService so the
 * URL the client receives is the same path Spring mounts here.
 *
 * <p>Also enables {@link UploadProperties} binding from application.yml.
 */
@Configuration
@EnableConfigurationProperties(UploadProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final ImageUploadService uploadService;

    public WebMvcConfig(ImageUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPattern = uploadService.getPublicPath().replaceAll("/+$", "") + "/**";
        String fsLocation = uploadService.getRootDir().toUri().toString();
        registry.addResourceHandler(urlPattern)
                .addResourceLocations(fsLocation)
                .setCachePeriod(3600);
    }
}
