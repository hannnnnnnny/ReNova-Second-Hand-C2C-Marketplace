package com.novacart.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CheckoutItemRequest(
        @NotNull(message = "Product is required.")
        Long productId,

        @Min(value = 1, message = "Quantity must be at least 1.")
        int quantity
) {
}
