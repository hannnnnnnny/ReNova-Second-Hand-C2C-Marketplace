package com.novacart.store.dto;

import com.novacart.store.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "Order status is required.")
        OrderStatus status
) {
}
