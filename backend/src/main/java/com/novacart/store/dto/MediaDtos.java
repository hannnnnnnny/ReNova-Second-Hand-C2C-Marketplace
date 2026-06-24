package com.novacart.store.dto;

import com.novacart.store.entity.MediaAsset;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

public final class MediaDtos {

    private MediaDtos() {}

    public record CreateUploadIntentRequest(
            @NotBlank @Size(max = 255) String fileName,
            @NotBlank @Size(max = 40) String contentType,
            @Positive @Max(10_485_760) long sizeBytes
    ) {}

    public record UploadIntentResponse(
            Long mediaId,
            String uploadUrl,
            Map<String, String> requiredHeaders,
            Instant expiresAt
    ) {}

    public record MediaResponse(
            Long id,
            String url,
            String contentType,
            Long sizeBytes,
            Integer width,
            Integer height,
            String status
    ) {
        public static MediaResponse from(MediaAsset asset, String url) {
            return new MediaResponse(
                    asset.getId(),
                    url,
                    asset.getContentType(),
                    asset.getStoredSizeBytes(),
                    asset.getWidth(),
                    asset.getHeight(),
                    asset.getStatus().name()
            );
        }
    }
}
