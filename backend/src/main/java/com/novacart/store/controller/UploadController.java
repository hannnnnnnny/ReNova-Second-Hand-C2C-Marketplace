package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.UploadDtos;
import com.novacart.store.security.CurrentUserService;
import com.novacart.store.service.ImageUploadService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final ImageUploadService uploadService;
    private final CurrentUserService currentUserService;

    public UploadController(ImageUploadService uploadService, CurrentUserService currentUserService) {
        this.uploadService = uploadService;
        this.currentUserService = currentUserService;
    }

    /**
     * Multipart upload. Field name is {@code files} so the frontend uses
     * a single FormData key per file.
     *
     * <p>Authentication is enforced server-side: anonymous callers cannot
     * write to disk via this endpoint.
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadDtos.UploadedImagesResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files
    ) {
        // Calling requireCurrentUser also rejects anonymous + missing
        // token cases — no anonymous file writes possible.
        currentUserService.requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(
                "Uploaded.",
                uploadService.store(files)
        ));
    }
}
