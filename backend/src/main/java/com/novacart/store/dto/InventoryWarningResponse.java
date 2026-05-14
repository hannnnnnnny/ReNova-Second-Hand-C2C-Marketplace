package com.novacart.store.dto;

public record InventoryWarningResponse(
        Long productId,
        String productName,
        String categoryName,
        int stockQuantity,
        boolean active
) {
}
