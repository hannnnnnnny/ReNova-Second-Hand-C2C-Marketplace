package com.novacart.store.service;

import com.novacart.store.security.SessionCookieService;
import com.novacart.store.repository.MediaAssetRepository;
import com.novacart.store.repository.TradeOrderRepository;
import com.novacart.store.repository.UserRepository;
import com.novacart.store.support.TestMediaAssets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CheckoutCorrectnessTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaAssetRepository mediaRepository;

    @Autowired
    private TradeOrderRepository orderRepository;

    @Autowired
    private OrderReservationExpiryService expiryService;

    private Session seller;
    private Session firstBuyer;
    private Session secondBuyer;
    private long listingId;

    @BeforeEach
    void setUpTrade() throws Exception {
        seller = registerAndLogin("Checkout Seller");
        firstBuyer = registerAndLogin("Checkout Buyer One");
        secondBuyer = registerAndLogin("Checkout Buyer Two");
        listingId = createListing(seller, "One-off checkout item");
    }

    @Test
    void repeatedCheckoutWithSameKeyAndPayloadReturnsTheOriginalOrder() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        MvcResult first = createOrder(firstBuyer, idempotencyKey, "1 First Street", status().isOk());
        long firstOrderId = dataId(first);

        createOrder(firstBuyer, idempotencyKey, "1 First Street", status().isOk())
                .getResponse();

        mockMvc.perform(get("/api/orders/{id}", firstOrderId).cookie(firstBuyer.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(firstOrderId));
    }

    @Test
    void reusingAnIdempotencyKeyForDifferentPayloadReturnsConflict() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        createOrder(firstBuyer, idempotencyKey, "1 First Street", status().isOk());
        createOrder(firstBuyer, idempotencyKey, "2 Different Street", status().isConflict());
    }

    @Test
    void secondBuyerCannotCreateAnotherActiveOrderForTheListing() throws Exception {
        createOrder(firstBuyer, UUID.randomUUID().toString(), "1 First Street", status().isOk());
        createOrder(secondBuyer, UUID.randomUUID().toString(), "2 Second Street", status().isConflict());
    }

    @Test
    void concurrentBuyersCannotBothReserveTheSameListing() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> concurrentOrderAttempt(firstBuyer, ready, start));
            var second = executor.submit(() -> concurrentOrderAttempt(secondBuyer, ready, start));
            ready.await();
            start.countDown();

            assertThat(List.of(first.get(), second.get()))
                    .containsExactlyInAnyOrder(200, 409);
        }
    }

    @Test
    void expiredPendingOrderReleasesTheListing() throws Exception {
        long orderId = dataId(createOrder(
                firstBuyer,
                UUID.randomUUID().toString(),
                "1 First Street",
                status().isOk()
        ));
        var order = orderRepository.findById(orderId).orElseThrow();
        order.setReservationExpiresAt(Instant.now().minusSeconds(1));
        orderRepository.saveAndFlush(order);

        assertThat(expiryService.expireBatch(Instant.now())).isEqualTo(1);

        mockMvc.perform(get("/api/orders/{id}", orderId).cookie(firstBuyer.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        mockMvc.perform(get("/api/public/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void browserCannotMarkAnOrderPaidWithoutAProviderWebhook() throws Exception {
        long orderId = dataId(createOrder(
                firstBuyer,
                UUID.randomUUID().toString(),
                "1 First Street",
                status().isOk()
        ));

        mockMvc.perform(post("/api/orders/{id}/pay", orderId)
                        .cookie(firstBuyer.cookie())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    private MvcResult createOrder(
            Session buyer,
            String idempotencyKey,
            String address,
            org.springframework.test.web.servlet.ResultMatcher expectedStatus
    ) throws Exception {
        return mockMvc.perform(post("/api/orders")
                        .cookie(buyer.cookie())
                        .with(csrf())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listingId", listingId,
                                "shippingName", "Checkout Buyer",
                                "shippingPhone", "+64 21 555 0199",
                                "shippingAddress", address,
                                "buyerNote", "Handle with care"
                        ))))
                .andExpect(expectedStatus)
                .andReturn();
    }

    private int concurrentOrderAttempt(Session buyer, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        return mockMvc.perform(post("/api/orders")
                        .cookie(buyer.cookie())
                        .with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listingId", listingId,
                                "shippingName", "Concurrent Buyer",
                                "shippingPhone", "+64 21 555 0199",
                                "shippingAddress", "1 Concurrent Street"
                        ))))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    private long createListing(Session owner, String title) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("description", "A unique persisted item used to verify checkout correctness.");
        payload.put("price", new BigDecimal("75.00"));
        payload.put("condition", "GOOD");
        payload.put("categoryId", firstCategoryId());
        payload.put("location", "Auckland");
        payload.put("negotiable", false);
        payload.put("shippingFee", new BigDecimal("5.00"));
        payload.put("mediaIds", List.of(TestMediaAssets.readyImage(
                owner.email(), userRepository, mediaRepository
        )));

        MvcResult result = mockMvc.perform(post("/api/listings")
                        .cookie(owner.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();
        return dataId(result);
    }

    private Session registerAndLogin(String displayName) throws Exception {
        String email = "checkout." + UUID.randomUUID() + "@example.test";
        String password = "T!" + UUID.randomUUID() + "a1";

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "displayName", displayName,
                                "password", password,
                                "location", "Auckland"
                        ))))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String prefix = SessionCookieService.COOKIE_NAME + "=";
        return new Session(new Cookie(
                SessionCookieService.COOKIE_NAME,
                setCookie.substring(prefix.length(), setCookie.indexOf(';'))
        ), email);
    }

    private long firstCategoryId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").get(0).path("id").asLong();
    }

    private long dataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private record Session(Cookie cookie, String email) {}
}
