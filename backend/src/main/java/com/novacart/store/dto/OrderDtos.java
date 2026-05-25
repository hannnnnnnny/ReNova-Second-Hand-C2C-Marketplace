package com.novacart.store.dto;

import com.novacart.store.entity.TradeOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public final class OrderDtos {

    private OrderDtos() {}

    public record CreateOrderRequest(
            @NotNull Long listingId,
            Long acceptedOfferId,
            @NotBlank @Size(max = 80) String shippingName,
            @NotBlank @Size(max = 40) String shippingPhone,
            @NotBlank @Size(max = 400) String shippingAddress,
            @Size(max = 500) String buyerNote
    ) {}

    public record ShipOrderRequest(
            @NotBlank @Size(max = 80) String carrier,
            @NotBlank @Size(max = 80) String trackingNumber
    ) {}

    public record CancelOrderRequest(
            @Size(max = 400) String reason
    ) {}

    public record OrderResponse(
            Long id,
            String orderNumber,
            Long listingId,
            String listingTitle,
            String listingCoverImageUrl,
            UserDtos.PublicUser buyer,
            UserDtos.PublicUser seller,
            BigDecimal agreedPrice,
            BigDecimal shippingFee,
            BigDecimal totalAmount,
            String shippingName,
            String shippingPhone,
            String shippingAddress,
            String buyerNote,
            String trackingNumber,
            String carrier,
            String status,
            Instant createdAt,
            Instant paidAt,
            Instant shippedAt,
            Instant deliveredAt,
            Instant completedAt,
            Instant cancelledAt,
            String cancelReason
    ) {
        public static OrderResponse from(TradeOrder o) {
            String cover = o.getListing().getImageUrls() != null && !o.getListing().getImageUrls().isEmpty()
                    ? o.getListing().getImageUrls().get(0) : null;
            return new OrderResponse(
                    o.getId(),
                    o.getOrderNumber(),
                    o.getListing().getId(),
                    o.getListing().getTitle(),
                    cover,
                    UserDtos.PublicUser.from(o.getBuyer()),
                    UserDtos.PublicUser.from(o.getSeller()),
                    o.getAgreedPrice(),
                    o.getShippingFee(),
                    o.getTotalAmount(),
                    o.getShippingName(),
                    o.getShippingPhone(),
                    o.getShippingAddress(),
                    o.getBuyerNote(),
                    o.getTrackingNumber(),
                    o.getCarrier(),
                    o.getStatus().name(),
                    o.getCreatedAt(),
                    o.getPaidAt(),
                    o.getShippedAt(),
                    o.getDeliveredAt(),
                    o.getCompletedAt(),
                    o.getCancelledAt(),
                    o.getCancelReason()
            );
        }
    }
}
