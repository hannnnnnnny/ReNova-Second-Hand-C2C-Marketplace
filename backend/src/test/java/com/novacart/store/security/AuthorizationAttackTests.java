package com.novacart.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novacart.store.dto.AuthDtos;
import com.novacart.store.entity.Listing;
import com.novacart.store.entity.ListingStatus;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ConversationRepository;
import com.novacart.store.repository.ListingRepository;
import com.novacart.store.repository.OfferRepository;
import com.novacart.store.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * GATE 2 evidence: every protected action is enforced server-side.
 *
 * <p>Each test plays the role of a hostile second user (or anon) and
 * tries to mutate or read another user's resource via direct HTTP.
 * Endpoints must refuse — independent of any UI hiding.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationAttackTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private OfferRepository offerRepository;
    @Autowired private ConversationRepository conversationRepository;
    @Autowired private ObjectMapper objectMapper;

    private String aliceEmail;
    private String bobEmail;
    private String aliceToken;
    private String bobToken;
    private Long aliceListingId;
    private Long categoryId;

    @BeforeEach
    void seed() throws Exception {
        // Unique emails so reruns don't collide with leftover state.
        aliceEmail = "alice-" + UUID.randomUUID() + "@gate2.test";
        bobEmail = "bob-" + UUID.randomUUID() + "@gate2.test";

        authService.signup(new AuthDtos.SignupRequest(aliceEmail, "Alice", "AlicePassword1!", "Auckland"));
        authService.signup(new AuthDtos.SignupRequest(bobEmail, "Bob", "BobPassword1!", "Wellington"));

        aliceToken = authService.login(new AuthDtos.LoginRequest(aliceEmail, "AlicePassword1!")).token();
        bobToken = authService.login(new AuthDtos.LoginRequest(bobEmail, "BobPassword1!")).token();

        categoryId = categoryRepository.findAll().getFirst().getId();

        // Alice posts a listing.
        String listingJson = """
                {
                  "title": "Alice's iPad",
                  "description": "11 inch, mint condition.",
                  "price": 320.00,
                  "categoryId": %d,
                  "condition": "LIKE_NEW",
                  "location": "Auckland",
                  "negotiable": true,
                  "shippingFee": 0.00,
                  "imageUrls": ["https://example.com/img1.jpg"]
                }
                """.formatted(categoryId);

        MvcResult created = mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listingJson))
                .andExpect(status().isOk())
                .andReturn();
        // Identify *this test's* listing by parsing the create response,
        // not by querying findAll() — earlier tests in the same context
        // have already posted listings with the same title.
        String body = created.getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(body).get("data");
        aliceListingId = data.get("id").asLong();
        assertThat(body).contains("Alice's iPad");
    }

    // ---------- Attack 1: Bob tries to EDIT Alice's listing ----------
    @Test
    void bob_cannot_update_alices_listing() throws Exception {
        String tamper = """
                { "title": "Pwned by Bob", "price": 1.00 }
                """;
        mockMvc.perform(put("/api/listings/" + aliceListingId)
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tamper))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));

        // And the listing on disk is untouched.
        Listing after = listingRepository.findById(aliceListingId).orElseThrow();
        assertThat(after.getTitle()).isEqualTo("Alice's iPad");
        assertThat(after.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    }

    // ---------- Attack 2: Bob tries to DELETE Alice's listing ----------
    @Test
    void bob_cannot_delete_alices_listing() throws Exception {
        mockMvc.perform(delete("/api/listings/" + aliceListingId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().is4xxClientError());

        Listing after = listingRepository.findById(aliceListingId).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    }

    // ---------- Attack 3: Anonymous user hits a protected endpoint ----------
    @Test
    void anon_cannot_post_listing() throws Exception {
        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anon_cannot_edit_or_delete_listing() throws Exception {
        mockMvc.perform(put("/api/listings/" + aliceListingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/listings/" + aliceListingId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anon_cannot_view_my_resources() throws Exception {
        mockMvc.perform(get("/api/listings/mine")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders/buying")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders/selling")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/conversations")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/offers/received")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    // ---------- Attack 4: forged Bearer token must not authenticate ----------
    @Test
    void forged_token_is_rejected() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + aliceToken.substring(0, aliceToken.length() - 1) + "X"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Attack 5: Bob makes an offer; Bob (not Alice) tries to accept it ----------
    @Test
    void buyer_cannot_accept_their_own_offer() throws Exception {
        String offerJson = """
                { "listingId": %d, "amount": 280.00, "message": "fair?" }
                """.formatted(aliceListingId);

        mockMvc.perform(post("/api/offers")
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(offerJson))
                .andExpect(status().isOk());

        // Resolve the actual offer id by listing id — each test's
        // listing id is unique (parsed from its own create response).
        Long offerId = offerRepository.findAll().stream()
                .filter(o -> o.getListing().getId().equals(aliceListingId))
                .findFirst().orElseThrow().getId();

        // Bob (the buyer) tries to accept his own offer — must be refused.
        mockMvc.perform(post("/api/offers/" + offerId + "/accept")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().is4xxClientError());

        // And Alice (the seller) can accept it — sanity check that the
        // refusal is about *who*, not about the offer being broken.
        mockMvc.perform(post("/api/offers/" + offerId + "/accept")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk());
    }

    // ---------- Attack 6: stranger reads a private conversation ----------
    @Test
    void stranger_cannot_read_someone_elses_conversation() throws Exception {
        // Bob starts a conversation with Alice about her listing.
        String startJson = """
                { "listingId": %d, "body": "still available?" }
                """.formatted(aliceListingId);
        mockMvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startJson))
                .andExpect(status().isOk());
        Long convId = conversationRepository.findAll().stream()
                .filter(c -> c.getListing().getId().equals(aliceListingId))
                .findFirst().orElseThrow().getId();

        // A third party (charlie) signs up and tries to read the thread.
        String charlieEmail = "charlie-" + UUID.randomUUID() + "@gate2.test";
        authService.signup(new AuthDtos.SignupRequest(charlieEmail, "Charlie", "CharliePassword1!", null));
        String charlieToken = authService.login(new AuthDtos.LoginRequest(charlieEmail, "CharliePassword1!")).token();

        mockMvc.perform(get("/api/conversations/" + convId)
                        .header("Authorization", "Bearer " + charlieToken))
                .andExpect(status().is4xxClientError());

        // Charlie also cannot inject a message into the thread.
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + charlieToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"hi from stranger\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ---------- Attack 7: public profile endpoint must NOT leak email ----------
    @Test
    void public_profile_does_not_leak_email() throws Exception {
        Long aliceUserId = listingRepository.findById(aliceListingId).orElseThrow()
                .getSeller().getId();

        MvcResult result = mockMvc.perform(get("/api/public/users/" + aliceUserId))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();

        assertThat(body).contains("\"displayName\":\"Alice\"");
        assertThat(body).doesNotContain("@gate2.test");
        assertThat(body.toLowerCase()).doesNotContain("\"email\"");
        assertThat(body.toLowerCase()).doesNotContain("password");
    }

}
