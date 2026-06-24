package com.novacart.store.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novacart.store.repository.MediaAssetRepository;
import com.novacart.store.repository.UserRepository;
import com.novacart.store.support.TestMediaAssets;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReNovaApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaAssetRepository mediaRepository;

    @Test
    void publicCatalogReadsDatabaseBackedCategoriesAndListings() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].name").isString());

        mockMvc.perform(get("/api/public/listings").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void validationErrorsReturnFieldLevelMessages() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").isString())
                .andExpect(jsonPath("$.errors[0].message").isString());
    }

    @Test
    void invalidCatalogEnumsReturnBusinessErrorsInsteadOfServerErrors() throws Exception {
        mockMvc.perform(get("/api/public/listings").param("condition", "BROKEN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("condition must be one of")));
    }

    @Test
    void authenticatedSessionUsesHttpOnlyCookieForProtectedRoutes() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));

        TestAccount account = register("Session Test User");
        Cookie sessionCookie = login(account.email(), account.password());

        mockMvc.perform(get("/api/auth/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(account.email()))
                .andExpect(jsonPath("$.data.displayName").value(account.displayName()));
    }

    @Test
    void listingCreationRequiresAuthAndPersistsToTheApi() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "Test ReNova lamp");
        payload.put("description", "A clean integration-test listing that proves the API is not a local mock.");
        payload.put("price", new BigDecimal("42.50"));
        payload.put("condition", "GOOD");
        payload.put("categoryId", firstCategoryId());
        payload.put("location", "Test City");
        payload.put("negotiable", true);
        payload.put("shippingFee", BigDecimal.ZERO);
        payload.put("mediaIds", List.of(999999L));

        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());

        TestAccount account = register("Listing Test User");
        Cookie sessionCookie = login(account.email(), account.password());
        payload.put("mediaIds", List.of(TestMediaAssets.readyImage(
                account.email(), userRepository, mediaRepository
        )));

        mockMvc.perform(post("/api/listings")
                        .cookie(sessionCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Test ReNova lamp"))
                .andExpect(jsonPath("$.data.seller.email").doesNotExist())
                .andExpect(jsonPath("$.data.seller.displayName").value(account.displayName()));
    }

    private TestAccount register(String displayName) throws Exception {
        String email = "account." + UUID.randomUUID() + "@example.test";
        String password = "T!" + UUID.randomUUID() + "a1";

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "displayName", displayName,
                                "password", password,
                                "location", "Test City"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        return new TestAccount(email, password, displayName);
    }

    private Cookie login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        String prefix = "RENOVA_SESSION=";
        return new Cookie("RENOVA_SESSION", setCookie.substring(prefix.length(), setCookie.indexOf(';')));
    }

    private long firstCategoryId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").get(0).path("id").asLong();
    }

    private record TestAccount(String email, String password, String displayName) {}
}
