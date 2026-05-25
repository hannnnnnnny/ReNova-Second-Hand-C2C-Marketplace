package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.ReviewDtos;
import com.novacart.store.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewDtos.ReviewResponse>> create(@Valid @RequestBody ReviewDtos.CreateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review submitted.", reviewService.create(request)));
    }

    @GetMapping("/public/users/{userId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewDtos.ReviewResponse>>> forUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews.", reviewService.listForUser(userId)));
    }

    @GetMapping("/orders/{orderId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewDtos.ReviewResponse>>> forOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews.", reviewService.listForOrder(orderId)));
    }
}
