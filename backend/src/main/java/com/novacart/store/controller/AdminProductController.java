package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.dto.ProductRequest;
import com.novacart.store.dto.ProductResponse;
import com.novacart.store.dto.ProductSearchRequest;
import com.novacart.store.entity.ProductStatus;
import com.novacart.store.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/admin/products")
@Validated
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> findProducts(
            @RequestParam(required = false) String search,
            @Positive(message = "Category ID must be positive.")
            @RequestParam(required = false) Long categoryId,
            @Positive(message = "Collection ID must be positive.")
            @RequestParam(required = false) Long collectionId,
            @RequestParam(required = false) String sizeFilter,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean saleOnly,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "name") String sort,
            @Min(value = 0, message = "Page index cannot be negative.")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1.")
            @Max(value = 60, message = "Page size cannot be greater than 60.")
            @RequestParam(defaultValue = "20") int size
    ) {
        ProductSearchRequest request = new ProductSearchRequest(
                search,
                categoryId,
                collectionId,
                sizeFilter,
                color,
                material,
                brand,
                season,
                tag,
                saleOnly,
                status,
                minPrice,
                maxPrice,
                availableOnly,
                sort,
                page,
                size
        );
        return ApiResponse.success("Products loaded successfully.", productService.searchAdminProducts(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findProduct(
            @Positive(message = "Product ID must be positive.")
            @PathVariable Long id
    ) {
        return ApiResponse.success("Product loaded successfully.", productService.findAdminProduct(id));
    }

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.success("Product created successfully.", productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @Positive(message = "Product ID must be positive.")
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponse.success("Product updated successfully.", productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(
            @Positive(message = "Product ID must be positive.")
            @PathVariable Long id
    ) {
        productService.deleteProduct(id);
        return ApiResponse.success("Product deleted successfully.");
    }
}
