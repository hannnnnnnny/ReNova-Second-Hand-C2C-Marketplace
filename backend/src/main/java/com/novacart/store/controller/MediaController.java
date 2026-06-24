package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.MediaDtos;
import com.novacart.store.service.MediaService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/media/upload-intents")
    public ResponseEntity<ApiResponse<MediaDtos.UploadIntentResponse>> createUploadIntent(
            @Valid @RequestBody MediaDtos.CreateUploadIntentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Upload intent created.",
                mediaService.createUploadIntent(request)
        ));
    }

    @PostMapping("/media/{id}/complete")
    public ResponseEntity<ApiResponse<MediaDtos.MediaResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Image processed.", mediaService.completeUpload(id)));
    }

    @GetMapping("/public/media/{id}")
    public ResponseEntity<Void> view(@PathVariable Long id) {
        URI location = mediaService.downloadUrl(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, location.toString())
                .build();
    }
}
