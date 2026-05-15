package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.CategoryResponse;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.dto.ProductResponse;
import com.novacart.store.dto.ProductSearchRequest;
import com.novacart.store.service.CategoryService;
import com.novacart.store.service.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@Validated
public class PublicCatalogController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public PublicCatalogController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> findCategories() {
        return ApiResponse.success("Categories loaded successfully.", categoryService.findPublicCategories());
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductResponse>> findProducts(
            @RequestParam(required = false) String search,
            @Positive(message = "Category ID must be positive.")
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "name") String sort,
            @Min(value = 0, message = "Page index cannot be negative.")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1.")
            @Max(value = 60, message = "Page size cannot be greater than 60.")
            @RequestParam(defaultValue = "12") int size
    ) {
        ProductSearchRequest request = new ProductSearchRequest(
                search,
                categoryId,
                null,
                minPrice,
                maxPrice,
                availableOnly,
                sort,
                page,
                size
        );
        return ApiResponse.success("Products loaded successfully.", productService.searchPublicProducts(request));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductResponse> findProduct(
            @Positive(message = "Product ID must be positive.")
            @PathVariable Long id
    ) {
        return ApiResponse.success("Product loaded successfully.", productService.findPublicProduct(id));
    }
}
