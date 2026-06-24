package com.novacart.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "novacart.media")
public record MediaProperties(
        String internalEndpoint,
        String uploadEndpoint,
        String accessKey,
        String secretKey,
        String bucket,
        String publicApiBaseUrl,
        long maxUploadBytes,
        long maxPixels,
        int uploadExpiryMinutes,
        int unattachedExpiryHours,
        int downloadExpiryMinutes
) {}
