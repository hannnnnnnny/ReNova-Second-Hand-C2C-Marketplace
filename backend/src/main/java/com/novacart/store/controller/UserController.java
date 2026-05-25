package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.ListingDtos;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.dto.UserDtos;
import com.novacart.store.entity.User;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.UserRepository;
import com.novacart.store.service.ListingService;
import com.novacart.store.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ListingService listingService;

    public UserController(UserService userService, UserRepository userRepository, ListingService listingService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.listingService = listingService;
    }

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserDtos.UserSummary>> me() {
        return ResponseEntity.ok(ApiResponse.success("Current user.", userService.currentUser()));
    }

    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<UserDtos.UserSummary>> updateMe(@Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated.", userService.updateProfile(request)));
    }

    @GetMapping("/public/users/{id}")
    public ResponseEntity<ApiResponse<UserDtos.PublicUser>> publicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Profile.", userService.getPublicProfile(id)));
    }

    @GetMapping("/public/users/{id}/listings")
    public ResponseEntity<ApiResponse<PageResponse<ListingDtos.ListingSummary>>> listings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User seller = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return ResponseEntity.ok(ApiResponse.success("Seller listings.", listingService.publicSellerListings(seller, page, size)));
    }
}
