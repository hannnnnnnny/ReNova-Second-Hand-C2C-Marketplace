package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.OfferDtos;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OfferDtos.OfferResponse>> create(@Valid @RequestBody OfferDtos.OfferCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Offer submitted.", offerService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfferDtos.OfferResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Offer.", offerService.get(id)));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<OfferDtos.OfferResponse>> accept(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Offer accepted.", offerService.accept(id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<OfferDtos.OfferResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Offer rejected.", offerService.reject(id)));
    }

    @PostMapping("/{id}/counter")
    public ResponseEntity<ApiResponse<OfferDtos.OfferResponse>> counter(
            @PathVariable Long id,
            @Valid @RequestBody OfferDtos.OfferCounterRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Counter sent.", offerService.counter(id, request)));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<OfferDtos.OfferResponse>> withdraw(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Offer withdrawn.", offerService.withdraw(id)));
    }

    @PostMapping("/{id}/accept-counter")
    public ResponseEntity<ApiResponse<OfferDtos.OfferResponse>> acceptCounter(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Counter accepted.", offerService.acceptCounter(id)));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<PageResponse<OfferDtos.OfferResponse>>> received(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Received offers.", offerService.receivedOffers(page, size)));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<PageResponse<OfferDtos.OfferResponse>>> sent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Sent offers.", offerService.sentOffers(page, size)));
    }
}
