package com.novacart.store.support;

import com.novacart.store.entity.MediaAsset;
import com.novacart.store.entity.MediaAssetStatus;
import com.novacart.store.repository.MediaAssetRepository;
import com.novacart.store.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class TestMediaAssets {

    private TestMediaAssets() {}

    public static long readyImage(
            String ownerEmail,
            UserRepository userRepository,
            MediaAssetRepository mediaRepository
    ) {
        MediaAsset asset = new MediaAsset();
        asset.setOwner(userRepository.findByEmailIgnoreCase(ownerEmail).orElseThrow());
        asset.setObjectKey("test/" + UUID.randomUUID());
        asset.setOriginalFilename("test-image.png");
        asset.setDeclaredContentType("image/png");
        asset.setContentType("image/png");
        asset.setSourceSizeBytes(128);
        asset.setStoredSizeBytes(128L);
        asset.setWidth(8);
        asset.setHeight(8);
        asset.setStatus(MediaAssetStatus.READY);
        asset.setCreatedAt(Instant.now());
        asset.setReadyAt(Instant.now());
        asset.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        return mediaRepository.save(asset).getId();
    }
}
