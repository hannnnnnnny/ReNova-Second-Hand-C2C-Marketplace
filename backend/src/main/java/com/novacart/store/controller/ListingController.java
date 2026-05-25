package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.ListingDtos;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.service.ListingService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/public/listings")
    public ResponseEntity<ApiResponse<PageResponse<ListingDtos.ListingSummary>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Listings.",
                listingService.search(keyword, categoryId, minPrice, maxPrice, condition, location, sort, page, size)));
    }

    @GetMapping("/public/listings/{id}")
    public ResponseEntity<ApiResponse<ListingDtos.ListingDetail>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Listing.", listingService.get(id)));
    }

    @PostMapping("/listings")
    public ResponseEntity<ApiResponse<ListingDtos.ListingDetail>> create(
            @Valid @RequestBody ListingDtos.ListingCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Listing created.", listingService.create(request)));
    }

    @PutMapping("/listings/{id}")
    public ResponseEntity<ApiResponse<ListingDtos.ListingDetail>> update(
            @PathVariable Long id,
            @Valid @RequestBody ListingDtos.ListingUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Listing updated.", listingService.update(id, request)));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long id) {
        listingService.remove(id);
        return ResponseEntity.ok(ApiResponse.success("Listing removed."));
    }

    @GetMapping("/listings/mine")
    public ResponseEntity<ApiResponse<PageResponse<ListingDtos.ListingSummary>>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("My listings.", listingService.mySellerListings(page, size)));
    }

    @PostMapping("/listings/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> toggleFavorite(@PathVariable Long id) {
        listingService.toggleFavorite(id);
        return ResponseEntity.ok(ApiResponse.success("Favorite toggled."));
    }

    @GetMapping("/listings/favorites")
    public ResponseEntity<ApiResponse<PageResponse<ListingDtos.ListingSummary>>> favorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Favorites.", listingService.myFavorites(page, size)));
    }
}
