package com.novacart.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "media_assets", indexes = {
        @Index(name = "idx_media_owner_status", columnList = "owner_id,status"),
        @Index(name = "idx_media_listing_position", columnList = "listing_id,display_order"),
        @Index(name = "idx_media_expiry", columnList = "status,expires_at")
})
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_media_owner"))
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", foreignKey = @ForeignKey(name = "fk_media_listing"))
    private Listing listing;

    @Column(name = "object_key", nullable = false, unique = true, length = 500)
    private String objectKey;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "declared_content_type", nullable = false, length = 40)
    private String declaredContentType;

    @Column(name = "content_type", length = 40)
    private String contentType;

    @Column(name = "source_size_bytes", nullable = false)
    private long sourceSizeBytes;

    @Column(name = "stored_size_bytes")
    private Long storedSizeBytes;

    private Integer width;

    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MediaAssetStatus status;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "ready_at")
    private Instant readyAt;

    @Column(name = "attached_at")
    private Instant attachedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getDeclaredContentType() { return declaredContentType; }
    public void setDeclaredContentType(String declaredContentType) { this.declaredContentType = declaredContentType; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSourceSizeBytes() { return sourceSizeBytes; }
    public void setSourceSizeBytes(long sourceSizeBytes) { this.sourceSizeBytes = sourceSizeBytes; }
    public Long getStoredSizeBytes() { return storedSizeBytes; }
    public void setStoredSizeBytes(Long storedSizeBytes) { this.storedSizeBytes = storedSizeBytes; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public MediaAssetStatus getStatus() { return status; }
    public void setStatus(MediaAssetStatus status) { this.status = status; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getReadyAt() { return readyAt; }
    public void setReadyAt(Instant readyAt) { this.readyAt = readyAt; }
    public Instant getAttachedAt() { return attachedAt; }
    public void setAttachedAt(Instant attachedAt) { this.attachedAt = attachedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
