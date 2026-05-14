package com.novacart.store.service.impl;

import com.novacart.store.dto.DashboardMetricsResponse;
import com.novacart.store.dto.InventoryWarningResponse;
import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.Product;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.CustomerOrderRepository;
import com.novacart.store.repository.ProductRepository;
import com.novacart.store.service.AdminDashboardService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CustomerOrderRepository orderRepository;

    public AdminDashboardServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CustomerOrderRepository orderRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public DashboardMetricsResponse getMetrics() {
        return new DashboardMetricsResponse(
                productRepository.count(),
                productRepository.countByActiveTrue(),
                categoryRepository.count(),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                productRepository.countByStockQuantityLessThanEqual(DEFAULT_LOW_STOCK_THRESHOLD),
                orderRepository.sumTotalAmountByStatusNot(OrderStatus.CANCELLED)
        );
    }

    @Override
    public List<InventoryWarningResponse> getInventoryWarnings(int threshold) {
        return productRepository.findAllByStockQuantityLessThanEqualOrderByStockQuantityAsc(threshold)
                .stream()
                .map(this::toWarning)
                .toList();
    }

    private InventoryWarningResponse toWarning(Product product) {
        return new InventoryWarningResponse(
                product.getId(),
                product.getName(),
                product.getCategory().getName(),
                product.getStockQuantity(),
                product.isActive()
        );
    }
}
