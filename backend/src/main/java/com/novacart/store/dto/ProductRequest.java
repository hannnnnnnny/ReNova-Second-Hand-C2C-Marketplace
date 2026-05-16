package com.novacart.store.dto;

import com.novacart.store.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "Product name is required.")
        @Size(max = 180, message = "Product name must be 180 characters or fewer.")
        String name,

        @Size(max = 200, message = "Product slug must be 200 characters or fewer.")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Product slug must use lowercase letters, numbers, and hyphens."
        )
        String slug,

        @Size(max = 80, message = "Product SKU must be 80 characters or fewer.")
        @Pattern(
                regexp = "^[A-Z0-9][A-Z0-9-]*$",
                message = "Product SKU must use uppercase letters, numbers, and hyphens."
        )
        String sku,

        @Size(max = 120, message = "Product brand must be 120 characters or fewer.")
        String brand,

        @NotBlank(message = "Product description is required.")
        @Size(max = 2_000, message = "Product description must be 2,000 characters or fewer.")
        String description,

        @NotNull(message = "Product price is required.")
        @DecimalMin(value = "0.01", message = "Product price must be greater than zero.")
        @Digits(integer = 10, fraction = 2, message = "Product price must use no more than 10 digits and 2 decimals.")
        BigDecimal price,

        @DecimalMin(value = "0.01", message = "Compare-at price must be greater than zero.")
        @Digits(integer = 10, fraction = 2, message = "Compare-at price must use no more than 10 digits and 2 decimals.")
        BigDecimal compareAtPrice,

        @NotNull(message = "Stock quantity is required.")
        @Min(value = 0, message = "Stock quantity cannot be negative.")
        Integer stockQuantity,

        @Min(value = 0, message = "Low-stock threshold cannot be negative.")
        Integer lowStockThreshold,

        @NotBlank(message = "Product image URL is required.")
        @Size(max = 600, message = "Product image URL must be 600 characters or fewer.")
        @Pattern(
                regexp = "^(https?://|/).+",
                message = "Product image URL must be an absolute HTTP URL or a local public asset path."
        )
        String imageUrl,

        @Size(max = 6, message = "Product image gallery can include up to 6 images.")
        List<@Pattern(
                regexp = "^(https?://|/).+",
                message = "Gallery image URL must be an absolute HTTP URL or a local public asset path."
        ) String> imageGallery,

        @Size(max = 12, message = "Product tags can include up to 12 values.")
        List<@Size(max = 80, message = "Product tag must be 80 characters or fewer.") String> tags,

        Boolean featured,

        ProductStatus status,

        Boolean active,

        @NotNull(message = "Product category is required.")
        Long categoryId
) {
}
