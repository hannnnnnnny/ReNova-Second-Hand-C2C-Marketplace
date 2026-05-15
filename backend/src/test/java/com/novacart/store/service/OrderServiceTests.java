package com.novacart.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novacart.store.dto.CheckoutItemRequest;
import com.novacart.store.dto.CheckoutRequest;
import com.novacart.store.dto.OrderResponse;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.Product;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrderDeductsStockAndCreatesOrderItems() {
        Product product = saveProduct("Test Desk Tray", "test-desk-tray", 3, "15.00");

        OrderResponse response = orderService.createOrder(new CheckoutRequest(
                "Morgan Lee",
                "morgan@example.com",
                "12 Market Street",
                "Auckland",
                "1010",
                "New Zealand",
                List.of(new CheckoutItemRequest(product.getId(), 2))
        ));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(1);
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void createOrderRejectsInsufficientStockWithoutChangingInventory() {
        Product product = saveProduct("Test Low Stock Cloth", "test-low-stock-cloth", 1, "9.00");

        CheckoutRequest request = new CheckoutRequest(
                "Morgan Lee",
                "morgan@example.com",
                "12 Market Street",
                "Auckland",
                "1010",
                "New Zealand",
                List.of(new CheckoutItemRequest(product.getId(), 2))
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Insufficient stock for Test Low Stock Cloth.");

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchangedProduct.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void updateStatusAllowsFulfillmentProgression() {
        Product product = saveProduct("Test Status Lamp", "test-status-lamp", 4, "22.00");
        OrderResponse order = createSingleItemOrder(product);

        OrderResponse paidOrder = orderService.updateStatus(order.id(), OrderStatus.PAID);
        OrderResponse processingOrder = orderService.updateStatus(order.id(), OrderStatus.PROCESSING);
        OrderResponse shippedOrder = orderService.updateStatus(order.id(), OrderStatus.SHIPPED);
        OrderResponse completedOrder = orderService.updateStatus(order.id(), OrderStatus.COMPLETED);

        assertThat(paidOrder.status()).isEqualTo(OrderStatus.PAID);
        assertThat(processingOrder.status()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(shippedOrder.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(completedOrder.status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void updateStatusAllowsCancellationBeforeShipment() {
        Product product = saveProduct("Test Cancel Tray", "test-cancel-tray", 4, "18.00");
        OrderResponse order = createSingleItemOrder(product);

        OrderResponse cancelledOrder = orderService.updateStatus(order.id(), OrderStatus.CANCELLED);

        assertThat(cancelledOrder.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void updateStatusRejectsTerminalStatusChanges() {
        Product product = saveProduct("Test Terminal Bowl", "test-terminal-bowl", 4, "24.00");
        OrderResponse order = createSingleItemOrder(product);

        orderService.updateStatus(order.id(), OrderStatus.CANCELLED);

        assertThatThrownBy(() -> orderService.updateStatus(order.id(), OrderStatus.PROCESSING))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Order status cannot change from Cancelled to Processing.");
    }

    private Product saveProduct(String name, String slug, int stockQuantity, String price) {
        Category category = categoryRepository.save(new Category(
                name + " Category",
                slug + "-category",
                "Category for checkout service tests.",
                true
        ));

        return productRepository.save(new Product(
                name,
                slug,
                "Product used by checkout service tests.",
                new BigDecimal(price),
                stockQuantity,
                "https://example.com/test-product.jpg",
                true,
                category
        ));
    }

    private OrderResponse createSingleItemOrder(Product product) {
        return orderService.createOrder(new CheckoutRequest(
                "Morgan Lee",
                "morgan@example.com",
                "12 Market Street",
                "Auckland",
                "1010",
                "New Zealand",
                List.of(new CheckoutItemRequest(product.getId(), 1))
        ));
    }
}
