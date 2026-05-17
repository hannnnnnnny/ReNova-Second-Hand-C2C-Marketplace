package com.novacart.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novacart.store.config.DataInitializer;
import com.novacart.store.dto.PromotionRequest;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.Product;
import com.novacart.store.entity.PromotionDiscountType;
import com.novacart.store.entity.PromotionTargetType;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ProductRepository;
import com.novacart.store.service.PromotionService;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionService promotionService;

    @Value("${novacart.security.jwt-secret}")
    private String jwtSecret;

    @Test
    void loginReturnsBearerTokenForDefaultAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@novacart.local",
                                  "password": "NovaCartAdmin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token", not(emptyString())))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value(DataInitializer.DEFAULT_ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void loginRejectsInvalidCredentialsWithUnauthorizedResponse() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@novacart.local",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email address or password."));
    }

    @Test
    void publicProductListingReturnsSeededProducts() throws Exception {
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(20)))
                .andExpect(jsonPath("$.data.content[0].name", not(emptyString())));
    }

    @Test
    void publicProductListingSupportsSearchFiltersAndPagination() throws Exception {
        mockMvc.perform(get("/api/public/products")
                        .param("search", "jacket")
                        .param("availableOnly", "true")
                        .param("sort", "price-low")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void publicProductListingSearchesFashionTags() throws Exception {
        Product product = saveProduct("Controller Active Weekend Pack", 6, "68.00", true);
        String uniqueTag = "controller-active-weekend-" + UUID.randomUUID().toString().substring(0, 8);
        product.setTags(List.of(uniqueTag, "equipment"));
        productRepository.save(product);

        mockMvc.perform(get("/api/public/products")
                        .param("search", uniqueTag)
                        .param("availableOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Controller Active Weekend Pack"));
    }

    @Test
    void saleFilterIncludesProductsDiscountedByActivePromotions() throws Exception {
        Product product = saveProduct("Controller Promotion Only Sneaker", 8, "90.00", true);
        Category category = product.getCategory();
        promotionService.createPromotion(new PromotionRequest(
                "Controller category sale",
                "API test promotion for sale filtering.",
                PromotionDiscountType.PERCENTAGE,
                new BigDecimal("15.00"),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(7),
                true,
                PromotionTargetType.CATEGORY,
                List.of(category.getSlug())
        ));

        mockMvc.perform(get("/api/public/products")
                        .param("search", "Promotion Only Sneaker")
                        .param("saleOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Controller Promotion Only Sneaker"))
                .andExpect(jsonPath("$.data.content[0].effectivePrice").value(76.50))
                .andExpect(jsonPath("$.data.content[0].discountPercent").value(15));
    }

    @Test
    void adminProductListingSupportsStatusFiltering() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("status", "DRAFT")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.content[0].status").value("DRAFT"));
    }

    @Test
    void adminProductCreationStoresExpandedCatalogFields() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Category category = categoryRepository.save(new Category(
                "Expanded Product Category " + suffix,
                "expanded-product-category-" + suffix,
                "Category for expanded product API tests.",
                true
        ));

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Controller Linen Field Jacket",
                                  "slug": "controller-linen-field-jacket-%s",
                                  "sku": "TEST-LINEN-JACKET-%s",
                                  "brand": "Aster Row",
                                  "description": "A test jacket with realistic fashion product metadata for admin catalog workflows.",
                                  "price": 44.00,
                                  "compareAtPrice": 52.00,
                                  "stockQuantity": 9,
                                  "lowStockThreshold": 3,
                                  "imageUrl": "/catalog/new-arrivals.svg",
                                  "imageGallery": [
                                    "/catalog/new-arrivals.svg",
                                    "/catalog/women.svg"
                                  ],
                                  "tags": ["linen", "workwear"],
                                  "featured": true,
                                  "status": "ACTIVE",
                                  "active": true,
                                  "categoryId": %d
                                }
                                """.formatted(suffix, suffix.toUpperCase(), category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("TEST-LINEN-JACKET-" + suffix.toUpperCase()))
                .andExpect(jsonPath("$.data.brand").value("Aster Row"))
                .andExpect(jsonPath("$.data.compareAtPrice").value(52.00))
                .andExpect(jsonPath("$.data.lowStockThreshold").value(3))
                .andExpect(jsonPath("$.data.imageGallery.length()").value(2))
                .andExpect(jsonPath("$.data.tags[0]").value("linen"))
                .andExpect(jsonPath("$.data.featured").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void checkoutCreatesOrderAndDeductsStock() throws Exception {
        Product product = saveProduct("Controller Checkout Jacket", 5, "22.00", true);

        mockMvc.perform(post("/api/public/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Morgan Lee",
                                  "customerEmail": "morgan@example.com",
                                  "shippingAddress": "12 Market Street",
                                  "city": "Auckland",
                                  "postalCode": "1010",
                                  "country": "New Zealand",
                                  "items": [
                                    {
                                      "productId": %d,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.orderNumber", not(emptyString())))
                .andExpect(jsonPath("$.data.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.data.subtotalAmount").value(44.00))
                .andExpect(jsonPath("$.data.shippingAmount").value(6.00))
                .andExpect(jsonPath("$.data.taxAmount").value(3.52))
                .andExpect(jsonPath("$.data.totalAmount").value(53.52));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void checkoutRejectsInsufficientStockWithoutDeductingInventory() throws Exception {
        Product product = saveProduct("Controller Low Stock Jacket", 1, "22.00", true);

        mockMvc.perform(post("/api/public/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Morgan Lee",
                                  "customerEmail": "morgan@example.com",
                                  "shippingAddress": "12 Market Street",
                                  "city": "Auckland",
                                  "postalCode": "1010",
                                  "country": "New Zealand",
                                  "items": [
                                    {
                                      "productId": %d,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Insufficient stock for Controller Low Stock Jacket."));

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchangedProduct.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void checkoutRejectsValidationErrorsWithFieldMessages() throws Exception {
        mockMvc.perform(post("/api/public/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "",
                                  "customerEmail": "not-an-email",
                                  "shippingAddress": "",
                                  "city": "",
                                  "postalCode": "",
                                  "country": "",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed. Please review the highlighted fields."))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void adminRouteRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void adminRouteRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication token is invalid."));
    }

    @Test
    void adminRouteRejectsExpiredBearerToken() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + expiredToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication token has expired."));
    }

    @Test
    void adminOrderStatusEndpointRejectsInvalidWorkflowTransition() throws Exception {
        Product product = saveProduct("Controller Status Jacket", 4, "19.00", true);
        Number orderId = createOrder(product).longValue();
        String token = adminToken();

        mockMvc.perform(patch("/api/admin/orders/{id}/status", orderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CANCELLED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        mockMvc.perform(patch("/api/admin/orders/{id}/status", orderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PROCESSING"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order status cannot change from Cancelled to Processing."));
    }

    @Test
    void adminInventoryMovementsShowCheckoutStockDeduction() throws Exception {
        Product product = saveProduct("Controller Movement Jacket", 4, "19.00", true);
        createOrder(product);

        mockMvc.perform(get("/api/admin/inventory/movements")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].type").value("ORDER_PLACED"));
    }

    private Product saveProduct(String name, int stockQuantity, String price, boolean active) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Category category = categoryRepository.save(new Category(
                name + " Category",
                "test-category-" + suffix,
                "Category for API controller tests.",
                true
        ));

        return productRepository.save(new Product(
                name,
                "test-product-" + suffix,
                "Product used by API controller tests.",
                new BigDecimal(price),
                stockQuantity,
                "https://example.com/test-product.jpg",
                active,
                category
        ));
    }

    private Number createOrder(Product product) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/public/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Morgan Lee",
                                  "customerEmail": "morgan@example.com",
                                  "shippingAddress": "12 Market Street",
                                  "city": "Auckland",
                                  "postalCode": "1010",
                                  "country": "New Zealand",
                                  "items": [
                                    {
                                      "productId": %d,
                                      "quantity": 1
                                    }
                                  ]
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    }

    private String adminToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@novacart.local",
                                  "password": "NovaCartAdmin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }

    private String expiredToken() {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(DataInitializer.DEFAULT_ADMIN_EMAIL)
                .issuedAt(Date.from(now.minus(3, ChronoUnit.HOURS)))
                .expiration(Date.from(now.minus(2, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }
}
