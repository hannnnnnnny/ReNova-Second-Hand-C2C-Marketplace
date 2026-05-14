package com.novacart.store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CheckoutRequest(
        @NotBlank(message = "Customer name is required.")
        @Size(max = 140, message = "Customer name must be 140 characters or fewer.")
        String customerName,

        @NotBlank(message = "Email address is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 180, message = "Email address must be 180 characters or fewer.")
        String customerEmail,

        @NotBlank(message = "Shipping address is required.")
        @Size(max = 220, message = "Shipping address must be 220 characters or fewer.")
        String shippingAddress,

        @NotBlank(message = "City is required.")
        @Size(max = 120, message = "City must be 120 characters or fewer.")
        String city,

        @NotBlank(message = "Postal code is required.")
        @Size(max = 40, message = "Postal code must be 40 characters or fewer.")
        String postalCode,

        @NotBlank(message = "Country is required.")
        @Size(max = 80, message = "Country must be 80 characters or fewer.")
        String country,

        @Valid
        @NotEmpty(message = "Your cart must include at least one item.")
        List<CheckoutItemRequest> items
) {
}
