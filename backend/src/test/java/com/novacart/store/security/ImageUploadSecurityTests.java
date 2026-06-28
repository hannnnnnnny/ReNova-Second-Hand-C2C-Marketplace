package com.novacart.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novacart.store.config.UploadProperties;
import com.novacart.store.dto.UploadDtos;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.service.ImageUploadService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image upload security contract.
 *
 * <p>These tests cover every gate the service is supposed to enforce.
 * If any of them ever starts failing the upload path has regressed.
 */
class ImageUploadSecurityTests {

    @TempDir Path tmp;
    private ImageUploadService service;
    private UploadProperties props;

    @BeforeEach
    void setUp() throws Exception {
        props = new UploadProperties();
        props.setDir(tmp.toString());
        props.setMaxFileBytes(1024); // 1 KB so we can exercise the size gate cheaply
        props.setMaxFilesPerRequest(3);
        props.setPublicPath("/uploads");
        service = new ImageUploadService(props);

        // call the @PostConstruct manually
        var m = ImageUploadService.class.getDeclaredMethod("init");
        m.setAccessible(true);
        m.invoke(service);
    }

    // ---------- magic-byte detection: accept three formats ----------

    @Test
    void accepts_jpeg_by_magic_bytes_even_when_client_lies_about_mime() throws Exception {
        byte[] jpeg = withHeader(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F'}, 64);
        // Client claims it's a PDF and gives an .exe filename — service must ignore both.
        MultipartFile file = new MockMultipartFile(
                "files", "totally-evil.exe", "application/pdf", jpeg);

        UploadDtos.UploadedImagesResponse r = service.store(List.of(file));

        assertThat(r.images()).hasSize(1);
        UploadDtos.UploadedImage uploaded = r.images().getFirst();
        assertThat(uploaded.contentType()).isEqualTo("image/jpeg");
        assertThat(uploaded.url()).startsWith("/uploads/");
        // The stored extension is derived from magic bytes, not the lying request.
        assertThat(uploaded.url()).endsWith(".jpg");
        // The bytes really landed on disk.
        Path written = tmp.resolve(uploaded.url().substring("/uploads/".length()));
        assertThat(Files.exists(written)).isTrue();
        assertThat(Files.size(written)).isEqualTo(uploaded.size());
    }

    @Test
    void accepts_png() {
        byte[] png = withHeader(new byte[]{
                (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
        }, 32);
        MultipartFile file = new MockMultipartFile("files", "ok.png", "image/png", png);
        UploadDtos.UploadedImagesResponse r = service.store(List.of(file));
        assertThat(r.images().getFirst().contentType()).isEqualTo("image/png");
        assertThat(r.images().getFirst().url()).endsWith(".png");
    }

    @Test
    void accepts_webp() {
        byte[] webp = withHeader(new byte[]{
                'R', 'I', 'F', 'F',
                0x00, 0x00, 0x00, 0x00,  // size, irrelevant
                'W', 'E', 'B', 'P'
        }, 48);
        MultipartFile file = new MockMultipartFile("files", "ok.webp", "image/webp", webp);
        UploadDtos.UploadedImagesResponse r = service.store(List.of(file));
        assertThat(r.images().getFirst().contentType()).isEqualTo("image/webp");
        assertThat(r.images().getFirst().url()).endsWith(".webp");
    }

    // ---------- magic-byte detection: reject hostile payloads ----------

    @Test
    void rejects_arbitrary_bytes_even_with_truthful_looking_filename() {
        byte[] notAnImage = "<!DOCTYPE html><script>alert('xss')</script>".getBytes();
        MultipartFile file = new MockMultipartFile(
                "files", "looks-fine.png", "image/png", notAnImage);

        assertThatThrownBy(() -> service.store(List.of(file)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("JPEG, PNG and WebP");
    }

    @Test
    void rejects_zip_disguised_as_jpeg() {
        // Real ZIP magic bytes: 50 4B 03 04
        byte[] zip = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00};
        MultipartFile file = new MockMultipartFile(
                "files", "harmless.jpg", "image/jpeg", zip);

        assertThatThrownBy(() -> service.store(List.of(file)))
                .isInstanceOf(BusinessRuleException.class);
    }

    // ---------- size limits ----------

    @Test
    void rejects_files_over_the_configured_byte_cap() {
        byte[] jpegHead = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        byte[] huge = withHeader(jpegHead, (int) props.getMaxFileBytes() + 100);
        MultipartFile file = new MockMultipartFile("files", "big.jpg", "image/jpeg", huge);

        assertThatThrownBy(() -> service.store(List.of(file)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void rejects_empty_file() {
        MultipartFile file = new MockMultipartFile("files", "empty.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> service.store(List.of(file)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("empty");
    }

    // ---------- per-request count cap ----------

    @Test
    void rejects_more_than_max_files_per_request() {
        byte[] jpeg = withHeader(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, 32);
        MultipartFile f1 = new MockMultipartFile("files", "a.jpg", "image/jpeg", jpeg);
        MultipartFile f2 = new MockMultipartFile("files", "b.jpg", "image/jpeg", jpeg);
        MultipartFile f3 = new MockMultipartFile("files", "c.jpg", "image/jpeg", jpeg);
        MultipartFile f4 = new MockMultipartFile("files", "d.jpg", "image/jpeg", jpeg);

        assertThatThrownBy(() -> service.store(List.of(f1, f2, f3, f4)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("most");
    }

    @Test
    void rejects_empty_upload_request() {
        assertThatThrownBy(() -> service.store(List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    // ---------- path safety ----------

    @Test
    void original_filename_never_drives_the_storage_path() throws Exception {
        byte[] jpeg = withHeader(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, 32);
        // path-traversal attempt as the original filename
        String evilName = "../../../etc/passwd.jpg";
        MultipartFile file = new MockMultipartFile("files", evilName, "image/jpeg", jpeg);

        UploadDtos.UploadedImagesResponse r = service.store(List.of(file));
        String url = r.images().getFirst().url();

        // URL must be of the form /uploads/<uuid>.jpg — no ".." or "etc"
        assertThat(url).doesNotContain("..").doesNotContain("etc").doesNotContain("passwd");
        assertThat(url).matches("^/uploads/[0-9a-f-]{36}\\.jpg$");

        // And the stored path is still inside the sandbox.
        Path written = tmp.resolve(url.substring("/uploads/".length()));
        assertThat(written.toAbsolutePath().normalize().startsWith(tmp.toAbsolutePath().normalize())).isTrue();
        assertThat(Files.exists(written)).isTrue();
    }

    @Test
    void original_filename_is_returned_sanitized_but_path_components_stripped() throws IOException {
        byte[] jpeg = withHeader(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, 32);
        MultipartFile file = new MockMultipartFile(
                "files", "a/b/c/photo.jpg", "image/jpeg", jpeg);

        UploadDtos.UploadedImagesResponse r = service.store(List.of(file));
        // Path components are not allowed in the returned originalName.
        assertThat(r.images().getFirst().originalName()).isEqualTo("photo.jpg");
    }

    // -- helpers --

    /** Build a buffer that starts with the given magic bytes and is padded to total length. */
    private byte[] withHeader(byte[] header, int totalLength) {
        byte[] b = new byte[Math.max(totalLength, header.length)];
        System.arraycopy(header, 0, b, 0, header.length);
        // fill the rest with deterministic bytes derived from a UUID so each
        // test still writes a distinct file, even if a previous run lingered
        byte[] tail = UUID.randomUUID().toString().getBytes();
        for (int i = header.length; i < b.length; i++) b[i] = tail[(i - header.length) % tail.length];
        return b;
    }
}
