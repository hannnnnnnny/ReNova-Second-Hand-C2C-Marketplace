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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

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
        order.setStatus(status);
        return toResponse(order);
    }

    private Map<Long, Integer> aggregateQuantities(List<CheckoutItemRequest> items) {
        Map<Long, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (CheckoutItemRequest item : items) {
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
