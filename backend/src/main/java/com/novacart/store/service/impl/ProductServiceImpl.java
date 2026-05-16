package com.novacart.store.service.impl;

import com.novacart.store.dto.CategoryResponse;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.dto.ProductRequest;
import com.novacart.store.dto.ProductResponse;
import com.novacart.store.dto.ProductSearchRequest;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.Product;
import com.novacart.store.entity.ProductStatus;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.exception.DuplicateResourceException;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ProductRepository;
import com.novacart.store.service.ProductService;
import com.novacart.store.service.SlugService;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SlugService slugService;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SlugService slugService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.slugService = slugService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findPublicProducts(Long categoryId) {
        return searchPublicProducts(new ProductSearchRequest(
                null,
                categoryId,
                null,
                null,
                null,
                false,
                "name",
                0,
                100
        )).content();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchPublicProducts(ProductSearchRequest request) {
        return searchProducts(request, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findPublicProduct(Long id) {
        Product product = productRepository.findByIdAndActiveTrueAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found."));
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAdminProducts() {
        return searchAdminProducts(new ProductSearchRequest(
                null,
                null,
                null,
                null,
                null,
                false,
                "name",
                0,
                100
        )).content();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchAdminProducts(ProductSearchRequest request) {
        return searchProducts(request, false);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findAdminProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found."));
        return toResponse(product);
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        String slug = slugService.createSlug(request.slug(), request.name());
        if (productRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("A product with this slug already exists.");
        }

        String sku = normalizeSku(request.sku(), slug);
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("A product with this SKU already exists.");
        }
        validateCompareAtPrice(request.price(), request.compareAtPrice());

        Category category = findCategory(request.categoryId());
        ProductStatus status = request.status() == null ? ProductStatus.ACTIVE : request.status();
        boolean active = request.active() == null ? status == ProductStatus.ACTIVE : request.active();
        Product product = new Product(
                request.name().trim(),
                slug,
                sku,
                normalizeText(request.brand(), "Nova Atelier"),
                request.description().trim(),
                request.price(),
                request.compareAtPrice(),
                request.stockQuantity(),
                request.lowStockThreshold() == null ? 5 : request.lowStockThreshold(),
                request.imageUrl().trim(),
                normalizeImageGallery(request.imageUrl(), request.imageGallery()),
                normalizeList(request.tags()),
                Boolean.TRUE.equals(request.featured()),
                status,
                active,
                category
        );
        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found."));

        String slug = slugService.createSlug(request.slug(), request.name());
        if (productRepository.existsBySlugAndIdNot(slug, id)) {
            throw new DuplicateResourceException("A product with this slug already exists.");
        }

        String sku = request.sku() == null || request.sku().isBlank()
                ? (product.getSku() == null ? normalizeSku(null, slug) : product.getSku())
                : normalizeSku(request.sku(), slug);
        if (productRepository.existsBySkuAndIdNot(sku, id)) {
            throw new DuplicateResourceException("A product with this SKU already exists.");
        }
        validateCompareAtPrice(request.price(), request.compareAtPrice());
        ProductStatus status = request.status() == null ? product.getStatus() : request.status();

        product.setName(request.name().trim());
        product.setSlug(slug);
        product.setSku(sku);
        product.setBrand(normalizeText(request.brand(), "Nova Atelier"));
        product.setDescription(request.description().trim());
        product.setPrice(request.price());
        product.setCompareAtPrice(request.compareAtPrice());
        product.setStockQuantity(request.stockQuantity());
        product.setLowStockThreshold(request.lowStockThreshold() == null ? product.getLowStockThreshold() : request.lowStockThreshold());
        product.setImageUrl(request.imageUrl().trim());
        product.setImageGallery(normalizeImageGallery(request.imageUrl(), request.imageGallery()));
        product.setTags(normalizeList(request.tags()));
        product.setFeatured(Boolean.TRUE.equals(request.featured()));
        product.setStatus(status);
        product.setActive(request.active() == null ? status == ProductStatus.ACTIVE : request.active());
        product.setCategory(findCategory(request.categoryId()));
        return toResponse(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found."));
        productRepository.delete(product);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category was not found."));
    }

    private PageResponse<ProductResponse> searchProducts(ProductSearchRequest request, boolean publicOnly) {
        validatePriceRange(request.minPrice(), request.maxPrice());
        Pageable pageable = PageRequest.of(
                Math.max(request.page(), 0),
                Math.max(1, Math.min(request.size(), 60)),
                resolveSort(request.sort())
        );
        Page<ProductResponse> products = productRepository.findAll(productSpecification(request, publicOnly), pageable)
                .map(this::toResponse);
        return PageResponse.from(products);
    }

    private Specification<Product> productSpecification(ProductSearchRequest request, boolean publicOnly) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var categoryJoin = root.join("category", JoinType.LEFT);

            if (publicOnly) {
                predicates.add(criteriaBuilder.isTrue(root.get("active")));
                predicates.add(criteriaBuilder.equal(root.get("status"), ProductStatus.ACTIVE));
            } else if (request.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
            }

            if (request.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), request.categoryId()));
            }

            if (request.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.minPrice()));
            }

            if (request.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.maxPrice()));
            }

            if (request.availableOnly()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stockQuantity"), 0));
            }

            String searchTerm = request.search() == null ? "" : request.search().trim().toLowerCase(Locale.ROOT);
            if (!searchTerm.isBlank()) {
                String pattern = "%" + searchTerm + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get("name")), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "name" : sort) {
            case "price-low", "price-asc" -> Sort.by("price").ascending().and(Sort.by("name").ascending());
            case "price-high", "price-desc" -> Sort.by("price").descending().and(Sort.by("name").ascending());
            case "stock" -> Sort.by("stockQuantity").descending().and(Sort.by("name").ascending());
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("name").ascending();
        };
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessRuleException("Minimum price cannot be greater than maximum price.");
        }
    }

    private String normalizeSku(String sku, String fallbackSource) {
        if (sku != null && !sku.isBlank()) {
            return sku.trim().toUpperCase(Locale.ROOT);
        }
        return "NC-" + fallbackSource.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private List<String> normalizeImageGallery(String primaryImageUrl, List<String> imageGallery) {
        List<String> images = normalizeList(imageGallery);
        String primaryImage = primaryImageUrl.trim();
        if (images.isEmpty()) {
            return List.of(primaryImage);
        }
        if (!images.contains(primaryImage)) {
            return java.util.stream.Stream.concat(java.util.stream.Stream.of(primaryImage), images.stream()).toList();
        }
        return images;
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private void validateCompareAtPrice(BigDecimal price, BigDecimal compareAtPrice) {
        if (compareAtPrice != null && compareAtPrice.compareTo(price) <= 0) {
            throw new BusinessRuleException("Compare-at price must be greater than the product price.");
        }
    }

    private ProductResponse toResponse(Product product) {
        Category category = product.getCategory();
        CategoryResponse categoryResponse = new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.isActive()
        );

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getBrand(),
                product.getDescription(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getStockQuantity(),
                product.getLowStockThreshold(),
                product.getImageUrl(),
                List.copyOf(product.getImageGallery()),
                List.copyOf(product.getTags()),
                product.isFeatured(),
                product.getStatus(),
                product.isActive(),
                categoryResponse
        );
    }
}
