package com.novacart.store.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novacart.store.security.SessionCookieService;
import com.novacart.store.service.media.MediaObjectStorage;
import jakarta.servlet.http.Cookie;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaUploadIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MediaObjectStorage storage;

    private Session owner;
    private Session stranger;

    @BeforeEach
    void setUp() throws Exception {
        reset(storage);
        owner = registerAndLogin("Media Owner");
        stranger = registerAndLogin("Media Stranger");
    }

    @Test
    void coreMarketplaceFlowPersistsValidatedMediaContactAndPurchase() throws Exception {
        byte[] png = png(8, 6);
        when(storage.createUploadTarget(any(), eq("image/png"), any(Duration.class)))
                .thenReturn(new MediaObjectStorage.UploadTarget(
                        URI.create("http://storage.test/upload-token"),
                        Map.of("Content-Type", "image/png")
                ));
        MvcResult intent = mockMvc.perform(post("/api/media/upload-intents")
                        .cookie(owner.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fileName", "camera.png",
                                "contentType", "image/png",
                                "sizeBytes", png.length
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("http://storage.test/upload-token"))
                .andReturn();
        long mediaId = objectMapper.readTree(intent.getResponse().getContentAsString())
                .path("data")
                .path("mediaId")
                .asLong();

        when(storage.read(any(), eq(10_485_760L))).thenReturn(png);
        mockMvc.perform(post("/api/media/{id}/complete", mediaId)
                        .cookie(owner.cookie())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"))
                .andExpect(jsonPath("$.data.width").value(8))
                .andExpect(jsonPath("$.data.height").value(6));
        verify(storage).put(any(), any(byte[].class), eq("image/png"));

        mockMvc.perform(post("/api/media/{id}/complete", mediaId)
                        .cookie(stranger.cookie())
                        .with(csrf()))
                .andExpect(status().isNotFound());

        long listingId = createListing(owner, mediaId, status().isOk());
        createListing(stranger, mediaId, status().isNotFound());

        mockMvc.perform(get("/api/public/listings")
                        .param("keyword", "Owned camera")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(listingId));

        when(storage.createDownloadUrl(any(), any(Duration.class)))
                .thenReturn(URI.create("http://storage.test/read-token"));
        mockMvc.perform(get("/api/public/media/{id}", mediaId))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://storage.test/read-token"));

        mockMvc.perform(get("/api/public/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mediaIds[0]").value(mediaId))
                .andExpect(jsonPath("$.data.imageUrls[0]").value(
                        "http://localhost:8080/api/public/media/" + mediaId
                ));

        mockMvc.perform(post("/api/conversations")
                        .cookie(stranger.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listingId", listingId,
                                "body", "Can I collect this camera tomorrow?"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversation.listingId").value(listingId));

        mockMvc.perform(post("/api/orders")
                        .cookie(stranger.cookie())
                        .with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listingId", listingId,
                                "shippingName", "Media Stranger",
                                "shippingPhone", "+64 21 555 0137",
                                "shippingAddress", "21 Queen Street, Auckland"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.listingId").value(listingId))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"));

        mockMvc.perform(get("/api/public/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESERVED"));
    }

    private long createListing(
            Session session,
            long mediaId,
            org.springframework.test.web.servlet.ResultMatcher expectedStatus
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/listings")
                        .cookie(session.cookie())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Owned camera " + UUID.randomUUID(),
                                "description", "A real persisted listing with a validated image.",
                                "price", new BigDecimal("95.00"),
                                "condition", "GOOD",
                                "categoryId", firstCategoryId(),
                                "negotiable", true,
                                "shippingFee", BigDecimal.ZERO,
                                "mediaIds", java.util.List.of(mediaId)
                        ))))
                .andExpect(expectedStatus)
                .andReturn();
        return result.getResponse().getStatus() == 200 ? dataId(result) : -1;
    }

    private Session registerAndLogin(String displayName) throws Exception {
        String email = "media." + UUID.randomUUID() + "@example.test";
        String password = "M!" + UUID.randomUUID() + "a1";
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
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        String setCookie = login.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String prefix = SessionCookieService.COOKIE_NAME + "=";
        return new Session(new Cookie(
                SessionCookieService.COOKIE_NAME,
                setCookie.substring(prefix.length(), setCookie.indexOf(';'))
        ));
    }

    private long firstCategoryId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/public/categories")).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").get(0).path("id").asLong();
    }

    private long dataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private byte[] png(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private record Session(Cookie cookie) {}
}
