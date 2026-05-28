package com.novacart.store.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final String DEMO_EMAIL = "ava@renova.local";
    private static final String DEMO_PASSWORD = "DemoPassword1!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicCatalogReadsSeededDataFromTheBackend() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].name").isString());

        mockMvc.perform(get("/api/public/listings").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].seller.displayName").isString())
                .andExpect(jsonPath("$.data.content[0].category.name").isString());
    }

    @Test
    void validationErrorsReturnFieldLevelMessages() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
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
    void authenticatedSessionUsesJwtForProtectedRoutes() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));

        String token = login(DEMO_EMAIL, DEMO_PASSWORD);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(DEMO_EMAIL))
                .andExpect(jsonPath("$.data.displayName").value("Ava Chen"));
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
        payload.put("imageUrls", List.of("https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=900"));

        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());

        String token = login(DEMO_EMAIL, DEMO_PASSWORD);

        mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Test ReNova lamp"))
                .andExpect(jsonPath("$.data.seller.email").doesNotExist())
                .andExpect(jsonPath("$.data.seller.displayName").value("Ava Chen"));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    private long firstCategoryId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").get(0).path("id").asLong();
    }
}
