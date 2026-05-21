package com.novacart.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novacart.store.dto.CheckoutItemRequest;
import com.novacart.store.dto.CheckoutRequest;
import com.novacart.store.dto.OrderResponse;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.PaymentStatus;
import com.novacart.store.entity.Product;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
        Product product = saveProduct("Test Cotton Jacket", "test-cotton-jacket", 3, "15.00");

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
        assertThat(response.orderNumber()).startsWith("NC-");
        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.subtotalAmount()).isEqualByComparingTo("30.00");
        assertThat(response.shippingAmount()).isEqualByComparingTo("6.00");
        assertThat(response.taxAmount()).isEqualByComparingTo("2.40");
        assertThat(response.totalAmount()).isEqualByComparingTo("38.40");
    }

    @Test
    void createOrderRejectsInsufficientStockWithoutChangingInventory() {
        Product product = saveProduct("Test Low Stock Tee", "test-low-stock-tee", 1, "9.00");

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
                .hasMessage("Insufficient stock for Test Low Stock Tee.");

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchangedProduct.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void createOrderReturnsExistingOrderForRepeatedIdempotencyKey() {
        Product product = saveProduct("Test Idempotent Shirt", "test-idempotent-shirt", 3, "20.00");
        CheckoutRequest request = new CheckoutRequest(
                "Morgan Lee",
                "morgan@example.com",
                "12 Market Street",
                "Auckland",
                "1010",
                "New Zealand",
                "STANDARD",
                "Demo Card Approved",
                "checkout-idempotency-key-1",
                false,
                List.of(new CheckoutItemRequest(product.getId(), 1))
        );

        OrderResponse firstResponse = orderService.createOrder(request);
        OrderResponse secondResponse = orderService.createOrder(request);

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(secondResponse.id()).isEqualTo(firstResponse.id());
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(2);
    }

    @Test
    void createOrderHandlesConcurrentRepeatedIdempotencyKey() throws Exception {
        Product product = saveProduct("Test Concurrent Shirt", "test-concurrent-shirt", 5, "20.00");
        CheckoutRequest request = new CheckoutRequest(
                "Morgan Lee",
                "morgan@example.com",
                "12 Market Street",
                "Auckland",
                "1010",
                "New Zealand",
                "STANDARD",
                "Demo Card Approved",
                "checkout-idempotency-key-concurrent",
                false,
                List.of(new CheckoutItemRequest(product.getId(), 1))
        );
        int workers = 6;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<OrderResponse>> futures = java.util.stream.IntStream.range(0, workers)
                .mapToObj(index -> executor.submit(() -> {
                    ready.countDown();
                    start.await(2, TimeUnit.SECONDS);
                    return orderService.createOrder(request);
                }))
                .toList();

        ready.await(2, TimeUnit.SECONDS);
        start.countDown();
        Set<Long> orderIds = new HashSet<>();
        for (Future<OrderResponse> future : futures) {
            orderIds.add(future.get(5, TimeUnit.SECONDS).id());
        }
        executor.shutdownNow();

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(orderIds).hasSize(1);
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(4);
    }

    @Test
    void createOrderRejectsDemoPaymentFailureWithoutChangingInventory() {
        Product product = saveProduct("Test Declined Payment Sneaker", "test-declined-payment-sneaker", 3, "20.00");
        CheckoutRequest request = new CheckoutRequest(
                "Morgan Lee",
                "morgan@example.com",
                "12 Market Street",
                "Auckland",
                "1010",
                "New Zealand",
                "STANDARD",
                "Demo Card Declined",
                "checkout-idempotency-key-2",
                true,
                List.of(new CheckoutItemRequest(product.getId(), 1))
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Demo payment was declined. Select Demo Card Approved and try again.");

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchangedProduct.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void updateStatusAllowsFulfillmentProgression() {
        Product product = saveProduct("Test Status Loafer", "test-status-loafer", 4, "22.00");
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
        Product product = saveProduct("Test Cancel Tote", "test-cancel-tote", 4, "18.00");
        OrderResponse order = createSingleItemOrder(product);

        OrderResponse cancelledOrder = orderService.updateStatus(order.id(), OrderStatus.CANCELLED);

        Product restoredProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(cancelledOrder.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(restoredProduct.getStockQuantity()).isEqualTo(4);
    }

    @Test
    void updateStatusRejectsTerminalStatusChanges() {
        Product product = saveProduct("Test Terminal Blazer", "test-terminal-blazer", 4, "24.00");
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
