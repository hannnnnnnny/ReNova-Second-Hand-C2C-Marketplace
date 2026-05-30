package com.novacart.store.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novacart.store.dto.AuthDtos;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * PHASE C / Phase A: codifies the buyer-seller core loop end-to-end as
 * a single integration test against the real Spring context, MockMvc
 * (which runs the full Spring Security filter chain), and the H2 DB.
 *
 * <p>Signup × 2 → Alice posts a listing → anyone browses and finds
 * it → Bob views it (view counter ticks) → Bob messages Alice →
 * Bob offers, Alice accepts, listing reserves → Bob checks out →
 * pay → ship → confirm → listing reaches SOLD and the order is
 * visible to both sides on cold re-fetch.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoreLoopFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void fullCoreLoop_signupPostBrowseMessageOfferOrderPayShipConfirm() throws Exception {
        // -- 1. signup --
        String aliceEmail = "alice-flow-" + UUID.randomUUID() + "@phasec.test";
        String bobEmail = "bob-flow-" + UUID.randomUUID() + "@phasec.test";
        authService.signup(new AuthDtos.SignupRequest(aliceEmail, "Alice Flow", "FlowPass1!", "Auckland"));
        authService.signup(new AuthDtos.SignupRequest(bobEmail, "Bob Flow", "FlowPass1!", "Wellington"));
        String aliceToken = authService.login(new AuthDtos.LoginRequest(aliceEmail, "FlowPass1!")).token();
        String bobToken = authService.login(new AuthDtos.LoginRequest(bobEmail, "FlowPass1!")).token();
        Long categoryId = categoryRepository.findAll().getFirst().getId();

        // -- 2. alice posts a listing --
        String uniqueTitle = "Phase C flow listing " + UUID.randomUUID();
        String listingJson = """
                {
                  "title": "%s",
                  "description": "End-to-end test listing.",
                  "price": 100.00,
                  "originalPrice": 250.00,
                  "categoryId": %d,
                  "condition": "GOOD",
                  "location": "Auckland",
                  "negotiable": true,
                  "shippingFee": 5.00,
                  "imageUrls": ["https://example.com/x.jpg"]
                }
                """.formatted(uniqueTitle, categoryId);
        MvcResult postResult = mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listingJson))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode created = objectMapper.readTree(postResult.getResponse().getContentAsString()).get("data");
        Long listingId = created.get("id").asLong();
        assertThat(created.get("status").asText()).isEqualTo("ACTIVE");

        // -- 3. anonymous browse finds the listing --
        MvcResult browse = mockMvc.perform(get("/api/public/listings")
                        .param("keyword", uniqueTitle)
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode browseData = objectMapper.readTree(browse.getResponse().getContentAsString()).get("data");
        assertThat(browseData.get("content").size()).isEqualTo(1);
        assertThat(browseData.get("content").get(0).get("title").asText()).isEqualTo(uniqueTitle);
        // and lazy associations are present (no LazyInitializationException)
        assertThat(browseData.get("content").get(0).get("category").get("name").asText()).isNotBlank();
        assertThat(browseData.get("content").get(0).get("seller").get("displayName").asText()).isEqualTo("Alice Flow");

        // -- 4. detail view counter increments --
        int v1 = openDetail(listingId).get("viewCount").asInt();
        int v2 = openDetail(listingId).get("viewCount").asInt();
        assertThat(v2).isGreaterThan(v1);

        // -- 5. bob starts a conversation; alice has 1 unread --
        String startConv = "{\"listingId\":" + listingId + ",\"body\":\"Hi! Still available?\"}";
        mockMvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startConv))
                .andExpect(status().isOk());
        MvcResult unread = mockMvc.perform(get("/api/conversations/unread-count")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk()).andReturn();
        long aliceUnread = objectMapper.readTree(unread.getResponse().getContentAsString())
                .get("data").get("count").asLong();
        assertThat(aliceUnread).isEqualTo(1L);

        // -- 6. offer → accept; listing reserves --
        String offerJson = "{\"listingId\":" + listingId + ",\"amount\":80.00,\"message\":\"$80?\"}";
        MvcResult offerResult = mockMvc.perform(post("/api/offers")
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(offerJson))
                .andExpect(status().isOk()).andReturn();
        Long offerId = objectMapper.readTree(offerResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        MvcResult accept = mockMvc.perform(post("/api/offers/" + offerId + "/accept")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk()).andReturn();
        assertThat(objectMapper.readTree(accept.getResponse().getContentAsString())
                .get("data").get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(openDetail(listingId).get("status").asText()).isEqualTo("RESERVED");

        // -- 7. checkout at the agreed price --
        String orderJson = """
                {
                  "listingId": %d,
                  "acceptedOfferId": %d,
                  "shippingName": "Bob Flow",
                  "shippingPhone": "+64 27 555 0100",
                  "shippingAddress": "42 Manners St, Wellington 6011",
                  "buyerNote": "After 6pm please"
                }
                """.formatted(listingId, offerId);
        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk()).andReturn();
        JsonNode order = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("data");
        Long orderId = order.get("id").asLong();
        assertThat(order.get("status").asText()).isEqualTo("PENDING_PAYMENT");
        assertThat(order.get("agreedPrice").decimalValue()).isEqualByComparingTo(new java.math.BigDecimal("80.00"));
        assertThat(order.get("totalAmount").decimalValue()).isEqualByComparingTo(new java.math.BigDecimal("85.00")); // 80 + 5 shipping

        // -- 8. state machine: pay → ship → confirm --
        assertThat(orderStatusAfter("/api/orders/" + orderId + "/pay", bobToken)).isEqualTo("PAID");
        assertThat(orderStatusAfter("/api/orders/" + orderId + "/ship", aliceToken,
                "{\"carrier\":\"NZ Post\",\"trackingNumber\":\"NZ12345\"}")).isEqualTo("SHIPPED");
        assertThat(orderStatusAfter("/api/orders/" + orderId + "/confirm-receipt", bobToken)).isEqualTo("COMPLETED");

        // -- 9. listing is SOLD; both sides see the order --
        assertThat(openDetail(listingId).get("status").asText()).isEqualTo("SOLD");
        MvcResult buying = mockMvc.perform(get("/api/orders/buying").param("size", "10")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk()).andReturn();
        MvcResult selling = mockMvc.perform(get("/api/orders/selling").param("size", "10")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk()).andReturn();
        assertThat(objectMapper.readTree(buying.getResponse().getContentAsString())
                .get("data").get("content").size()).isGreaterThan(0);
        assertThat(objectMapper.readTree(selling.getResponse().getContentAsString())
                .get("data").get("content").size()).isGreaterThan(0);

        // -- 10. cold re-fetch confirms persistence --
        MvcResult coldBuyer = mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk()).andReturn();
        MvcResult coldSeller = mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk()).andReturn();
        assertThat(objectMapper.readTree(coldBuyer.getResponse().getContentAsString())
                .get("data").get("status").asText()).isEqualTo("COMPLETED");
        assertThat(objectMapper.readTree(coldSeller.getResponse().getContentAsString())
                .get("data").get("status").asText()).isEqualTo("COMPLETED");
    }

    private JsonNode openDetail(Long listingId) throws Exception {
        MvcResult r = mockMvc.perform(get("/api/public/listings/" + listingId))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("data");
    }

    private String orderStatusAfter(String path, String token) throws Exception {
        return orderStatusAfter(path, token, null);
    }

    private String orderStatusAfter(String path, String token, String body) throws Exception {
        var req = post(path).header("Authorization", "Bearer " + token);
        if (body != null) req = req.contentType(MediaType.APPLICATION_JSON).content(body);
        MvcResult r = mockMvc.perform(req).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString())
                .get("data").get("status").asText();
    }
}
