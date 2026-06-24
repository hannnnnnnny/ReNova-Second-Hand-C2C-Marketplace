package com.novacart.store.service;

import com.novacart.store.config.MediaProperties;
import com.novacart.store.dto.MediaDtos;
import com.novacart.store.entity.Listing;
import com.novacart.store.entity.MediaAsset;
import com.novacart.store.entity.MediaAssetStatus;
import com.novacart.store.entity.User;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.exception.InvalidMediaException;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.MediaAssetRepository;
import com.novacart.store.security.CurrentUserService;
import com.novacart.store.service.media.ImageSanitizer;
import com.novacart.store.service.media.MediaObjectStorage;
import com.novacart.store.service.media.SanitizedImage;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final MediaAssetRepository mediaRepository;
    private final CurrentUserService currentUserService;
    private final MediaObjectStorage storage;
    private final MediaProperties properties;
    private final ImageSanitizer sanitizer;

    public MediaService(
            MediaAssetRepository mediaRepository,
            CurrentUserService currentUserService,
            MediaObjectStorage storage,
            MediaProperties properties
    ) {
        this.mediaRepository = mediaRepository;
        this.currentUserService = currentUserService;
        this.storage = storage;
        this.properties = properties;
        this.sanitizer = new ImageSanitizer(properties.maxUploadBytes(), properties.maxPixels());
    }

    @Transactional
    public MediaDtos.UploadIntentResponse createUploadIntent(MediaDtos.CreateUploadIntentRequest request) {
        User owner = currentUserService.requireCurrentUser();
        String contentType = normalizeType(request.contentType());
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidMediaException("Only JPEG, PNG, and WebP images are supported.");
        }
        if (request.sizeBytes() > properties.maxUploadBytes()) {
            throw new InvalidMediaException("Image exceeds the upload size limit.");
        }

        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(properties.uploadExpiryMinutes()));
        MediaAsset asset = new MediaAsset();
        asset.setOwner(owner);
        asset.setObjectKey("pending/" + owner.getId() + "/" + UUID.randomUUID());
        asset.setOriginalFilename(safeFileName(request.fileName()));
        asset.setDeclaredContentType(contentType);
        asset.setSourceSizeBytes(request.sizeBytes());
        asset.setStatus(MediaAssetStatus.PENDING);
        asset.setCreatedAt(Instant.now());
        asset.setExpiresAt(expiresAt);
        mediaRepository.save(asset);

        MediaObjectStorage.UploadTarget target = storage.createUploadTarget(
                asset.getObjectKey(),
                contentType,
                Duration.ofMinutes(properties.uploadExpiryMinutes())
        );
        return new MediaDtos.UploadIntentResponse(
                asset.getId(),
                target.url().toString(),
                target.requiredHeaders(),
                expiresAt
        );
    }

    @Transactional(noRollbackFor = InvalidMediaException.class)
    public MediaDtos.MediaResponse completeUpload(Long mediaId) {
        User owner = currentUserService.requireCurrentUser();
        MediaAsset asset = requireOwned(mediaId, owner);
        if (asset.getStatus() != MediaAssetStatus.PENDING) {
            throw new BusinessRuleException("This upload is not awaiting completion.");
        }
        if (asset.getExpiresAt() == null || !asset.getExpiresAt().isAfter(Instant.now())) {
            reject(asset);
            throw new InvalidMediaException("Upload intent has expired.");
        }

        try {
            byte[] source = storage.read(asset.getObjectKey(), properties.maxUploadBytes());
            if (source.length != asset.getSourceSizeBytes()) {
                throw new InvalidMediaException("Uploaded image size does not match the upload intent.");
            }
            SanitizedImage clean = sanitizer.sanitize(source, asset.getDeclaredContentType());
            storage.put(asset.getObjectKey(), clean.bytes(), clean.contentType());

            asset.setContentType(clean.contentType());
            asset.setStoredSizeBytes((long) clean.bytes().length);
            asset.setWidth(clean.width());
            asset.setHeight(clean.height());
            asset.setStatus(MediaAssetStatus.READY);
            asset.setReadyAt(Instant.now());
            asset.setExpiresAt(Instant.now().plus(Duration.ofHours(properties.unattachedExpiryHours())));
            return MediaDtos.MediaResponse.from(asset, publicUrl(asset.getId()));
        } catch (InvalidMediaException exception) {
            reject(asset);
            throw exception;
        }
    }

    @Transactional
    public void attach(Listing listing, User owner, List<Long> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty() || mediaIds.size() > 8) {
            throw new InvalidMediaException("A listing requires between one and eight images.");
        }
        if (new HashSet<>(mediaIds).size() != mediaIds.size()) {
            throw new InvalidMediaException("Each listing image can only be selected once.");
        }

        List<MediaAsset> selected = mediaIds.stream()
                .map(id -> requireOwned(id, owner))
                .toList();
        for (MediaAsset asset : selected) {
            boolean ready = asset.getStatus() == MediaAssetStatus.READY;
            boolean alreadyOnListing = asset.getStatus() == MediaAssetStatus.ATTACHED
                    && asset.getListing() != null
                    && asset.getListing().getId().equals(listing.getId());
            if (!ready && !alreadyOnListing) {
                throw new InvalidMediaException("Every selected image must be ready and owned by the seller.");
            }
        }

        Instant now = Instant.now();
        List<MediaAsset> previous = mediaRepository.findByListingOrderByDisplayOrderAsc(listing);
        for (MediaAsset asset : previous) {
            if (!mediaIds.contains(asset.getId())) {
                asset.setListing(null);
                asset.setDisplayOrder(null);
                asset.setAttachedAt(null);
                asset.setStatus(MediaAssetStatus.READY);
                asset.setExpiresAt(now.plus(Duration.ofHours(properties.unattachedExpiryHours())));
            }
        }

        List<String> urls = new ArrayList<>(selected.size());
        for (int index = 0; index < selected.size(); index++) {
            MediaAsset asset = selected.get(index);
            asset.setListing(listing);
            asset.setDisplayOrder(index);
            asset.setAttachedAt(now);
            asset.setStatus(MediaAssetStatus.ATTACHED);
            asset.setExpiresAt(null);
            urls.add(publicUrl(asset.getId()));
        }
        listing.setImageUrls(urls);
    }

    @Transactional(readOnly = true)
    public List<Long> mediaIdsFor(Listing listing) {
        return mediaRepository.findByListingOrderByDisplayOrderAsc(listing)
                .stream()
                .map(MediaAsset::getId)
                .toList();
    }

    @Transactional(readOnly = true)
    public URI downloadUrl(Long mediaId) {
        MediaAsset asset = mediaRepository.findById(mediaId)
                .filter(candidate -> candidate.getStatus() == MediaAssetStatus.ATTACHED)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found."));
        return storage.createDownloadUrl(
                asset.getObjectKey(),
                Duration.ofMinutes(properties.downloadExpiryMinutes())
        );
    }

    @Scheduled(fixedDelayString = "${novacart.media.cleanup-scan-ms:60000}")
    @Transactional
    public void cleanExpiredUploads() {
        List<MediaAsset> expired = mediaRepository.findTop100ByStatusInAndExpiresAtBeforeOrderByExpiresAtAsc(
                List.of(MediaAssetStatus.PENDING, MediaAssetStatus.READY, MediaAssetStatus.REJECTED),
                Instant.now()
        );
        for (MediaAsset asset : expired) {
            storage.delete(asset.getObjectKey());
            mediaRepository.delete(asset);
        }
    }

    private MediaAsset requireOwned(Long mediaId, User owner) {
        return mediaRepository.findByIdAndOwner(mediaId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found."));
    }

    private void reject(MediaAsset asset) {
        asset.setStatus(MediaAssetStatus.REJECTED);
        asset.setExpiresAt(Instant.now().plus(Duration.ofHours(properties.unattachedExpiryHours())));
    }

    private String publicUrl(Long mediaId) {
        String base = properties.publicApiBaseUrl().replaceAll("/+$", "");
        return base + "/public/media/" + mediaId;
    }

    private String normalizeType(String contentType) {
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String safeFileName(String fileName) {
        String normalized = fileName.trim().replace('\\', '/');
        String baseName = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (baseName.isBlank()) {
            throw new InvalidMediaException("Image filename is required.");
        }
        return baseName;
    }
}
