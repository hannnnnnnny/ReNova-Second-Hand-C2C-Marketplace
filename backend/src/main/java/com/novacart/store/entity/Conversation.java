package com.novacart.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "conversations",
        uniqueConstraints = @UniqueConstraint(name = "uk_conversation_listing_buyer", columnNames = {"listing_id", "buyer_id"}),
        indexes = {
                @Index(name = "idx_conversation_seller", columnList = "seller_id"),
                @Index(name = "idx_conversation_last_message", columnList = "last_message_at")
        })
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conversation_listing"))
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conversation_buyer"))
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conversation_seller"))
    private User seller;

    @Column(name = "last_message_preview", length = 280)
    private String lastMessagePreview;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    @Column(name = "buyer_unread_count", nullable = false)
    private int buyerUnreadCount;

    @Column(name = "seller_unread_count", nullable = false)
    private int sellerUnreadCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Conversation() {
        this.buyerUnreadCount = 0;
        this.sellerUnreadCount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }
    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public int getBuyerUnreadCount() { return buyerUnreadCount; }
    public void setBuyerUnreadCount(int buyerUnreadCount) { this.buyerUnreadCount = buyerUnreadCount; }
    public int getSellerUnreadCount() { return sellerUnreadCount; }
    public void setSellerUnreadCount(int sellerUnreadCount) { this.sellerUnreadCount = sellerUnreadCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
