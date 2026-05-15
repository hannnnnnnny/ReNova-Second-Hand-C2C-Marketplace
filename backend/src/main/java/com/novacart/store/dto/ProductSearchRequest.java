package com.novacart.store.dto;

import com.novacart.store.entity.ProductStatus;
import java.math.BigDecimal;

public record ProductSearchRequest(
        String search,
        Long categoryId,
        ProductStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        boolean availableOnly,
        String sort,
        int page,
        int size
) {
}
