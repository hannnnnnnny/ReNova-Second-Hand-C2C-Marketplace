package com.novacart.store.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
class ServerAuthorizationTests {

    private static final String SESSION_COOKIE = "RENOVA_SESSION";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sessionCookieIsHttpOnlyAndStateChangesRequireCsrf() throws Exception {
        TestAccount account = register("Cookie Security User");
        Session session = login(account);

        mockMvc.perform(get("/api/auth/me").cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(account.email()));

        mockMvc.perform(post("/api/listings")
                        .cookie(session.cookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listingPayload("Missing CSRF listing"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.cookie().getValue()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotEditOrDeleteAnotherUsersListing() throws Exception {
        Session seller = login(register("Listing Owner"));
        long listingId = createListing(seller, "Owner's original title");
        Session outsider = login(register("Listing Outsider"));

        mockMvc.perform(put("/api/listings/{id}", listingId)
                        .cookie(outsider.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Hijacked title"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/listings/{id}", listingId)
                        .cookie(outsider.cookie())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/public/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Owner's original title"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void thirdPartyCannotReadOrMutatePrivateTradeResources() throws Exception {
        TestAccount sellerAccount = register("Private Seller");
        Session seller = login(sellerAccount);
        long listingId = createListing(seller, "Private trade listing");

        TestAccount buyerAccount = register("Private Buyer");
        Session buyer = login(buyerAccount);
        long conversationId = startConversation(buyer, listingId);
        long offerId = createOffer(buyer, listingId);
        long orderId = createOrder(buyer, listingId);

        TestAccount outsiderAccount = register("Private Outsider");
        Session outsider = login(outsiderAccount);

        mockMvc.perform(get("/api/conversations/{id}", conversationId).cookie(outsider.cookie()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders/{id}", orderId).cookie(outsider.cookie()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders/{id}/reviews", orderId).cookie(outsider.cookie()))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/offers/{id}/accept", offerId)
                        .cookie(outsider.cookie())
                        .with(csrf()))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/orders/{id}/ship", orderId)
                        .cookie(buyer.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carrier", "Test Carrier",
                                "trackingNumber", "TEST-123"
                        ))))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/orders/{id}/pay", orderId)
                        .cookie(seller.cookie())
                        .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/users/me").cookie(outsider.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(outsiderAccount.email()))
                .andExpect(jsonPath("$.data.email").value(org.hamcrest.Matchers.not(buyerAccount.email())));

        mockMvc.perform(get("/api/public/users/{id}", sellerAccount.userId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/admin/probe").cookie(outsider.cookie()))
                .andExpect(status().isForbidden());
    }

    private TestAccount register(String displayName) throws Exception {
        String email = "gate2." + UUID.randomUUID() + "@example.test";
        String password = "T!" + UUID.randomUUID() + "a1";

        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "displayName", displayName,
                                "password", password,
                                "location", "Test City"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return new TestAccount(email, password, displayName, root.path("data").path("user").path("id").asLong());
    }

    private Session login(TestAccount account) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", account.email(),
                                "password", account.password()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.expiresAt").isString())
                .andReturn();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        org.assertj.core.api.Assertions.assertThat(setCookie)
                .contains(SESSION_COOKIE + "=")
                .contains("HttpOnly")
                .contains("SameSite=Lax")
                .contains("Path=/api");
        String value = setCookie.substring((SESSION_COOKIE + "=").length(), setCookie.indexOf(';'));
        return new Session(new Cookie(SESSION_COOKIE, value));
    }

    private long createListing(Session session, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/listings")
                        .cookie(session.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listingPayload(title))))
                .andExpect(status().isOk())
                .andReturn();
        return dataId(result);
    }

    private long startConversation(Session session, long listingId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/conversations")
                        .cookie(session.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listingId", listingId,
                                "body", "Is this item still available?"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("conversation").path("id").asLong();
    }

    private long createOrder(Session session, long listingId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .cookie(session.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listingId", listingId,
                                "shippingName", "Test Buyer",
                                "shippingPhone", "+64 21 555 0101",
                                "shippingAddress", "1 Test Street, Auckland"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        return dataId(result);
    }

    private long createOffer(Session session, long listingId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/offers")
                        .cookie(session.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listingId", listingId,
                                "amount", new BigDecimal("25.00"),
                                "message", "A test offer"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        return dataId(result);
    }

    private Map<String, Object> listingPayload(String title) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("description", "A database-backed listing used to verify server authorization boundaries.");
        payload.put("price", new BigDecimal("42.50"));
        payload.put("condition", "GOOD");
        payload.put("categoryId", firstCategoryId());
        payload.put("location", "Test City");
        payload.put("negotiable", true);
        payload.put("shippingFee", BigDecimal.ZERO);
        payload.put("imageUrls", List.of("https://images.example.test/listing.jpg"));
        return payload;
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

    private record TestAccount(String email, String password, String displayName, long userId) {}

    private record Session(Cookie cookie) {}
}
