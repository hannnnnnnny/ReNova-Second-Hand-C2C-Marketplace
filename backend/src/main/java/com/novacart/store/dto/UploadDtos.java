package com.novacart.store.dto;

import java.util.List;

public final class UploadDtos {

    private UploadDtos() {}

    /** Returned for a single uploaded image. */
    public record UploadedImage(
            String url,
            String contentType,
            long size,
            String originalName
    ) {}

    /** Returned for a batch upload request. */
    public record UploadedImagesResponse(
            List<UploadedImage> images
    ) {}
}
