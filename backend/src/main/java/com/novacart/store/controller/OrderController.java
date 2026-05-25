package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.OrderDtos;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.service.OrderService;
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
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDtos.OrderResponse>> create(@Valid @RequestBody OrderDtos.CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order created.", orderService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDtos.OrderResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order.", orderService.get(id)));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<OrderDtos.OrderResponse>> pay(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment recorded.", orderService.pay(id)));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<OrderDtos.OrderResponse>> ship(
            @PathVariable Long id,
            @Valid @RequestBody OrderDtos.ShipOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Order shipped.", orderService.ship(id, request)));
    }

    @PostMapping("/{id}/confirm-receipt")
    public ResponseEntity<ApiResponse<OrderDtos.OrderResponse>> confirmReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Receipt confirmed.", orderService.confirmReceipt(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDtos.OrderResponse>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) OrderDtos.CancelOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled.", orderService.cancel(id, request)));
    }

    @GetMapping("/buying")
    public ResponseEntity<ApiResponse<PageResponse<OrderDtos.OrderResponse>>> buying(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Buying.", orderService.listAsBuyer(status, page, size)));
    }

    @GetMapping("/selling")
    public ResponseEntity<ApiResponse<PageResponse<OrderDtos.OrderResponse>>> selling(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Selling.", orderService.listAsSeller(status, page, size)));
    }
}
