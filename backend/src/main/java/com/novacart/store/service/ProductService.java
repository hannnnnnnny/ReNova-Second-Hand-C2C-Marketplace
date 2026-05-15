package com.novacart.store.service;

import com.novacart.store.dto.PageResponse;
import com.novacart.store.dto.ProductRequest;
import com.novacart.store.dto.ProductResponse;
import com.novacart.store.dto.ProductSearchRequest;
import java.util.List;

public interface ProductService {

    List<ProductResponse> findPublicProducts(Long categoryId);

    PageResponse<ProductResponse> searchPublicProducts(ProductSearchRequest request);

    ProductResponse findPublicProduct(Long id);

    List<ProductResponse> findAdminProducts();

    PageResponse<ProductResponse> searchAdminProducts(ProductSearchRequest request);

    ProductResponse findAdminProduct(Long id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
