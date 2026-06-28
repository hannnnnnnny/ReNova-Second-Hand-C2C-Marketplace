package com.novacart.store.service;

import com.novacart.store.config.UploadProperties;
import com.novacart.store.dto.UploadDtos;
import com.novacart.store.exception.BusinessRuleException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores user-uploaded listing images on the local filesystem.
 *
 * <p>Security contract — every uploaded byte goes through these gates
 * before it lands on disk:
 * <ol>
 *   <li>file count limit per request</li>
 *   <li>per-file size limit, re-checked from the actual byte length
 *       (does not trust the multipart layer alone)</li>
 *   <li>image type detected by sniffing the first bytes
 *       (does not trust the client-supplied Content-Type or filename
 *       extension)</li>
 *   <li>stored filename is a fresh UUID with the extension derived
 *       from the sniffed type — original filename is never used as
 *       part of the path</li>
 *   <li>resolved path is verified to stay inside the configured root
 *       directory (defense in depth against future code that might
 *       weaken filename generation)</li>
 * </ol>
 */
@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    private final UploadProperties props;
    private Path rootDir;

    public ImageUploadService(UploadProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() throws IOException {
        rootDir = Paths.get(props.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(rootDir);
        log.info("Image upload root directory ready: {}", rootDir);
    }

    public UploadDtos.UploadedImagesResponse store(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessRuleException("Select at least one image to upload.");
        }
        if (files.size() > props.getMaxFilesPerRequest()) {
            throw new BusinessRuleException(
                    "At most " + props.getMaxFilesPerRequest() + " images can be uploaded at once.");
        }

        List<UploadDtos.UploadedImage> out = new ArrayList<>(files.size());
        for (MultipartFile mf : files) {
            out.add(storeOne(mf));
        }
        return new UploadDtos.UploadedImagesResponse(out);
    }

    private UploadDtos.UploadedImage storeOne(MultipartFile mf) {
        if (mf == null || mf.isEmpty()) {
            throw new BusinessRuleException("Image file is empty.");
        }
        long size = mf.getSize();
        if (size <= 0) {
            throw new BusinessRuleException("Image file is empty.");
        }
        if (size > props.getMaxFileBytes()) {
            throw new BusinessRuleException(
                    "Image exceeds the " + (props.getMaxFileBytes() / (1024 * 1024)) + " MB limit.");
        }

        byte[] bytes;
        try {
            bytes = mf.getBytes();
        } catch (IOException e) {
            throw new BusinessRuleException("Could not read uploaded file.");
        }
        if (bytes.length != size) {
            // multipart layer returned a different length than .getSize() —
            // refuse rather than guess.
            throw new BusinessRuleException("Image upload was truncated.");
        }
        if (bytes.length > props.getMaxFileBytes()) {
            throw new BusinessRuleException(
                    "Image exceeds the " + (props.getMaxFileBytes() / (1024 * 1024)) + " MB limit.");
        }

        ImageKind kind = detectKind(bytes);
        if (kind == null) {
            throw new BusinessRuleException(
                    "Only JPEG, PNG and WebP images are accepted.");
        }

        String filename = UUID.randomUUID() + "." + kind.extension;
        Path target = rootDir.resolve(filename).normalize();
        // Defense in depth: target must stay strictly inside rootDir.
        if (!target.startsWith(rootDir)) {
            throw new BusinessRuleException("Refusing to write outside the upload directory.");
        }

        try {
            Files.copy(mf.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to write upload to {}: {}", target, e.toString());
            throw new BusinessRuleException("Could not save the uploaded image.");
        }

        String url = props.getPublicPath().replaceAll("/+$", "") + "/" + filename;
        // Filename returned to the client is *sanitized*, not the raw one.
        // We expose the original name so the UI can show "photo3.jpg" in
        // the thumbnail caption — but it never touches the filesystem path.
        String safeOriginalName = sanitizeOriginalName(mf.getOriginalFilename());
        return new UploadDtos.UploadedImage(url, kind.mediaType, bytes.length, safeOriginalName);
    }

    /**
     * Magic-byte image sniffing. Covers the three formats we accept.
     * Returns null on no match — caller turns that into a 400.
     */
    private ImageKind detectKind(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return new ImageKind("jpg", "image/jpeg");
        }
        if (b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return new ImageKind("png", "image/png");
        }
        if (b.length >= 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return new ImageKind("webp", "image/webp");
        }
        return null;
    }

    private String sanitizeOriginalName(String raw) {
        if (raw == null || raw.isBlank()) return "image";
        String name = raw.replaceAll("[\\r\\n\\t]", "")
                          .replaceAll("[\\\\/]+", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        if (name.length() > 80) name = name.substring(0, 80);
        return name;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public String getPublicPath() {
        return props.getPublicPath();
    }

    private record ImageKind(String extension, String mediaType) {}
}
