package com.novacart.store.dto;

import com.novacart.store.entity.Conversation;
import com.novacart.store.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class MessageDtos {

    private MessageDtos() {}

    public record StartConversationRequest(
            @NotNull Long listingId,
            @NotBlank @Size(max = 2000) String body
    ) {}

    public record SendMessageRequest(
            @NotBlank @Size(max = 2000) String body
    ) {}

    public record MessageResponse(
            Long id,
            Long conversationId,
            Long senderId,
            String senderName,
            String body,
            Instant createdAt,
            Instant readAt
    ) {
        public static MessageResponse from(Message m) {
            return new MessageResponse(
                    m.getId(),
                    m.getConversation().getId(),
                    m.getSender().getId(),
                    m.getSender().getDisplayName(),
                    m.getBody(),
                    m.getCreatedAt(),
                    m.getReadAt()
            );
        }
    }

    public record ConversationSummary(
            Long id,
            Long listingId,
            String listingTitle,
            String listingCoverImageUrl,
            String listingStatus,
            UserDtos.PublicUser counterparty,
            String role,
            String lastMessagePreview,
            Instant lastMessageAt,
            int unreadCount
    ) {
        public static ConversationSummary from(Conversation c, Long currentUserId) {
            boolean isBuyer = c.getBuyer().getId().equals(currentUserId);
            String cover = c.getListing().getImageUrls() != null && !c.getListing().getImageUrls().isEmpty()
                    ? c.getListing().getImageUrls().get(0) : null;
            return new ConversationSummary(
                    c.getId(),
                    c.getListing().getId(),
                    c.getListing().getTitle(),
                    cover,
                    c.getListing().getStatus().name(),
                    UserDtos.PublicUser.from(isBuyer ? c.getSeller() : c.getBuyer()),
                    isBuyer ? "BUYER" : "SELLER",
                    c.getLastMessagePreview(),
                    c.getLastMessageAt(),
                    isBuyer ? c.getBuyerUnreadCount() : c.getSellerUnreadCount()
            );
        }
    }

    public record ConversationDetail(
            ConversationSummary conversation,
            List<MessageResponse> messages
    ) {}
}
