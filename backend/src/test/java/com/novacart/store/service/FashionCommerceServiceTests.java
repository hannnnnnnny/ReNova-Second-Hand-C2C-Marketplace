package com.novacart.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novacart.store.dto.CheckoutItemRequest;
import com.novacart.store.dto.CheckoutRequest;
import com.novacart.store.dto.OrderResponse;
import com.novacart.store.dto.PromotionRequest;
import com.novacart.store.dto.RefundRequestCreateRequest;
import com.novacart.store.dto.RefundStatusUpdateRequest;
import com.novacart.store.dto.SupportTicketRequest;
import com.novacart.store.dto.SupportTicketUpdateRequest;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.CustomerOrder;
import com.novacart.store.entity.PaymentStatus;
import com.novacart.store.entity.Product;
import com.novacart.store.entity.PromotionDiscountType;
import com.novacart.store.entity.PromotionTargetType;
import com.novacart.store.entity.RefundStatus;
import com.novacart.store.entity.SupportIssueType;
import com.novacart.store.entity.SupportTicketStatus;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.CustomerOrderRepository;
import com.novacart.store.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FashionCommerceServiceTests {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private CustomerCareService customerCareService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerOrderRepository orderRepository;

    @Test
    void percentagePromotionAppliesToCategoryAndLeavesUnmatchedProductsAtFullPrice() {
        Category category = saveCategory("Promotion Jackets");
        Product jacket = saveProduct("Promotion Linen Jacket", category, "100.00");
        Product unrelated = saveProduct("Promotion Leather Belt", saveCategory("Promotion Accessories"), "40.00");

        promotionService.createPromotion(new PromotionRequest(
                "Spring jacket event",
                "Seasonal test discount for jacket category.",
                PromotionDiscountType.PERCENTAGE,
                new BigDecimal("20.00"),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(7),
                true,
                PromotionTargetType.CATEGORY,
                List.of(category.getSlug())
        ));

        PromotionService.DiscountQuote jacketQuote = promotionService.quote(jacket);
        PromotionService.DiscountQuote unrelatedQuote = promotionService.quote(unrelated);

        assertThat(jacketQuote.effectivePrice()).isEqualByComparingTo("80.00");
        assertThat(jacketQuote.discountAmount()).isEqualByComparingTo("20.00");
        assertThat(jacketQuote.discountPercent()).isEqualTo(20);
        assertThat(unrelatedQuote.effectivePrice()).isEqualByComparingTo("40.00");
        assertThat(unrelatedQuote.discountAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void fixedAmountPromotionCannotRemoveTheEntireTargetPrice() {
        Product product = saveProduct("Promotion Price Guard Tee", saveCategory("Promotion Tees"), "30.00");

        PromotionRequest request = new PromotionRequest(
                "Invalid fixed discount",
                "Fixed discount that would zero a product price.",
                PromotionDiscountType.FIXED_AMOUNT,
                new BigDecimal("30.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                true,
                PromotionTargetType.SELECTED_PRODUCTS,
                List.of(String.valueOf(product.getId()))
        );

        assertThatThrownBy(() -> promotionService.createPromotion(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Fixed discounts must be lower than every targeted product price.");
    }

    @Test
    void supportTicketsCanBeCreatedAndMovedThroughAdminReview() {
        var created = customerCareService.createSupportTicket(new SupportTicketRequest(
                SupportIssueType.SHIPPING_ISSUE,
                "NC-TEST-0001",
                "support-flow@example.com",
                "Morgan Lee",
                "The delivery tracking has not moved for several days."
        ));

        var updated = customerCareService.updateSupportTicket(
                created.id(),
                new SupportTicketUpdateRequest(
                        SupportTicketStatus.WAITING_FOR_CUSTOMER,
                        "Asked the customer to confirm the delivery address."
                )
        );

        assertThat(created.status()).isEqualTo(SupportTicketStatus.OPEN);
        assertThat(updated.status()).isEqualTo(SupportTicketStatus.WAITING_FOR_CUSTOMER);
        assertThat(updated.internalNotes()).contains("confirm the delivery address");
    }

    @Test
    void refundRequestUpdatesOrderRefundAndPaymentStatusesWhenApproved() {
        Product product = saveProduct("Refund Flow Crossbody Bag", saveCategory("Refund Bags"), "75.00");
        OrderResponse order = orderService.createOrder(new CheckoutRequest(
                "Morgan Lee",
                "refund-flow@example.com",
                "12 Market Street",
                "Auckland",
                "1010",
                "New Zealand",
                List.of(new CheckoutItemRequest(product.getId(), "M", "Black", 1))
        ));

        var refund = customerCareService.createRefundRequest(new RefundRequestCreateRequest(
                order.orderNumber(),
                "refund-flow@example.com",
                "The strap arrived damaged and needs review."
        ));

        assertThat(refund.status()).isEqualTo(RefundStatus.REQUESTED);
        assertThatThrownBy(() -> customerCareService.createRefundRequest(new RefundRequestCreateRequest(
                order.orderNumber(),
                "refund-flow@example.com",
                "Duplicate request."
        ))).isInstanceOf(BusinessRuleException.class)
                .hasMessage("This order already has an open refund request.");

        var approved = customerCareService.updateRefundRequest(
                refund.id(),
                new RefundStatusUpdateRequest(RefundStatus.APPROVED, "Approved after photo review.")
        );
        CustomerOrder updatedOrder = orderRepository.findWithItemsById(order.id()).orElseThrow();

        assertThat(approved.status()).isEqualTo(RefundStatus.APPROVED);
        assertThat(updatedOrder.getRefundStatus()).isEqualTo(RefundStatus.APPROVED);
        assertThat(updatedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    private Category saveCategory(String name) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return categoryRepository.save(new Category(
                name + " " + suffix,
                slug(name) + "-" + suffix,
                "Category for fashion commerce service tests.",
                true
        ));
    }

    private Product saveProduct(String name, Category category, String price) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Product product = new Product(
                name,
                slug(name) + "-" + suffix,
                "Fashion commerce service test product.",
                new BigDecimal(price),
                12,
                "https://example.com/test-fashion-product.jpg",
                true,
                category
        );
        product.setTags(List.of("test-fashion", "service"));
        product.setSizes(List.of("S", "M", "L"));
        product.setColors(List.of("Black", "Ivory"));
        return productRepository.save(product);
    }

    private String slug(String value) {
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
