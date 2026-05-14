package com.novacart.store.dto;

import java.math.BigDecimal;

public record DashboardMetricsResponse(
        long totalProducts,
        long activeProducts,
        long totalCategories,
        long totalOrders,
        long pendingOrders,
        long lowStockProducts,
        BigDecimal revenue
) {
}
