package com.novacart.store.service.impl;

import com.novacart.store.dto.CheckoutItemRequest;
import com.novacart.store.dto.CheckoutRequest;
import com.novacart.store.dto.OrderItemResponse;
import com.novacart.store.dto.OrderResponse;
import com.novacart.store.entity.CustomerOrder;
import com.novacart.store.entity.OrderItem;
import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.Product;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.CustomerOrderRepository;
import com.novacart.store.repository.ProductRepository;
import com.novacart.store.service.OrderService;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, List<OrderStatus>> ALLOWED_STATUS_TRANSITIONS = buildStatusTransitions();

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(CustomerOrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public OrderResponse createOrder(CheckoutRequest request) {
        Map<Long, Integer> quantitiesByProduct = aggregateQuantities(request.items());

        CustomerOrder order = new CustomerOrder(
                request.customerName().trim(),
                request.customerEmail().trim(),
                request.shippingAddress().trim(),
                request.city().trim(),
                request.postalCode().trim(),
                request.country().trim()
        );

        for (Map.Entry<Long, Integer> entry : quantitiesByProduct.entrySet()) {
            Product product = productRepository.findByIdForUpdate(entry.getKey())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException("Product was not found."));

            int requestedQuantity = entry.getValue();
            if (product.getStockQuantity() < requestedQuantity) {
                throw new BusinessRuleException("Insufficient stock for " + product.getName() + ".");
            }

            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), requestedQuantity));
        }

        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long id) {
        CustomerOrder order = findOrderWithItems(id);
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAdminOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        CustomerOrder order = findOrderWithItems(id);
        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);
        return toResponse(order);
    }

    private static Map<OrderStatus, List<OrderStatus>> buildStatusTransitions() {
        Map<OrderStatus, List<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(OrderStatus.PENDING, List.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PAID, List.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PROCESSING, List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.SHIPPED, List.of(OrderStatus.COMPLETED));
        transitions.put(OrderStatus.COMPLETED, List.of());
        transitions.put(OrderStatus.CANCELLED, List.of());
        return Map.copyOf(transitions);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus nextStatus) {
        if (nextStatus == null) {
            throw new BusinessRuleException("Order status is required.");
        }

        if (currentStatus == nextStatus) {
            return;
        }

        List<OrderStatus> allowedStatuses = ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus, List.of());
        if (!allowedStatuses.contains(nextStatus)) {
            throw new BusinessRuleException("Order status cannot change from "
                    + formatStatus(currentStatus) + " to " + formatStatus(nextStatus) + ".");
        }
    }

    private String formatStatus(OrderStatus status) {
        String label = status.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }

    private Map<Long, Integer> aggregateQuantities(List<CheckoutItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessRuleException("Your cart must include at least one item.");
        }

        Map<Long, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (CheckoutItemRequest item : items) {
            if (item.productId() == null || item.productId() <= 0) {
                throw new BusinessRuleException("Product ID must be positive.");
            }
            if (item.quantity() < 1) {
                throw new BusinessRuleException("Quantity must be at least 1.");
            }
            quantitiesByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantitiesByProduct;
    }

    private CustomerOrder findOrderWithItems(Long id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found."));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getShippingAddress(),
                order.getCity(),
                order.getPostalCode(),
                order.getCountry(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
