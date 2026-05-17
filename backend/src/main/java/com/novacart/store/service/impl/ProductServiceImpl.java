package com.novacart.store.service.impl;

import com.novacart.store.dto.CategoryResponse;
import com.novacart.store.dto.CollectionResponse;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.dto.ProductRequest;
import com.novacart.store.dto.ProductResponse;
import com.novacart.store.dto.ProductSearchRequest;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.FashionCollection;
import com.novacart.store.entity.GenderTarget;
import com.novacart.store.entity.Product;
import com.novacart.store.entity.ProductStatus;
import com.novacart.store.entity.Promotion;
import com.novacart.store.entity.PromotionTargetType;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.exception.DuplicateResourceException;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.FashionCollectionRepository;
import com.novacart.store.repository.ProductRepository;
import com.novacart.store.repository.PromotionRepository;
import com.novacart.store.service.ProductService;
import com.novacart.store.service.PromotionService;
import com.novacart.store.service.SlugService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
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
    private final FashionCollectionRepository collectionRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;
    private final SlugService slugService;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            FashionCollectionRepository collectionRepository,
            PromotionRepository promotionRepository,
            PromotionService promotionService,
            SlugService slugService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.collectionRepository = collectionRepository;
        this.promotionRepository = promotionRepository;
        this.promotionService = promotionService;
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
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                "name",
                0,
                60
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
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                "name",
                0,
                60
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
        FashionCollection collection = findCollection(request.collectionId());
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
                normalizeList(request.sizes()),
                normalizeList(request.colors()),
                clean(request.material()),
                clean(request.careInstructions()),
                clean(request.season()),
                request.genderTarget(),
                Boolean.TRUE.equals(request.featured()),
                status,
                active,
                category,
                collection
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
        product.setSizes(normalizeList(request.sizes()));
        product.setColors(normalizeList(request.colors()));
        product.setMaterial(clean(request.material()));
        product.setCareInstructions(clean(request.careInstructions()));
        product.setSeason(clean(request.season()));
        product.setGenderTarget(request.genderTarget() == null ? GenderTarget.UNISEX : request.genderTarget());
        product.setFeatured(Boolean.TRUE.equals(request.featured()));
        product.setStatus(status);
        product.setActive(request.active() == null ? status == ProductStatus.ACTIVE : request.active());
        product.setCategory(findCategory(request.categoryId()));
        product.setCollection(findCollection(request.collectionId()));
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

    private FashionCollection findCollection(Long id) {
        if (id == null) {
            return null;
        }
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection was not found."));
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
        List<Promotion> activePromotions = Boolean.TRUE.equals(request.saleOnly())
                ? promotionRepository.findActiveOn(LocalDate.now())
                : List.of();

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
            Join<Product, FashionCollection> collectionJoin = root.join("collection", JoinType.LEFT);

            if (publicOnly) {
                predicates.add(criteriaBuilder.isTrue(root.get("active")));
                predicates.add(criteriaBuilder.equal(root.get("status"), ProductStatus.ACTIVE));
            } else if (request.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
            }

            if (request.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), request.categoryId()));
            }

            if (request.collectionId() != null) {
                predicates.add(criteriaBuilder.equal(collectionJoin.get("id"), request.collectionId()));
            }

            if (!isBlank(request.sizeFilter())) {
                var sizeJoin = root.join("sizes", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(sizeJoin.as(String.class)), normalize(request.sizeFilter())));
            }

            if (!isBlank(request.color())) {
                var colorJoin = root.join("colors", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(colorJoin.as(String.class)), normalize(request.color())));
            }

            if (!isBlank(request.material())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("material")), "%" + normalize(request.material()) + "%"));
            }

            if (!isBlank(request.brand())) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("brand")), normalize(request.brand())));
            }

            if (!isBlank(request.season())) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("season")), normalize(request.season())));
            }

            if (!isBlank(request.tag())) {
                var tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(tagJoin.as(String.class)), normalize(request.tag())));
            }

            if (Boolean.TRUE.equals(request.saleOnly())) {
                var saleTagJoin = root.join("tags", JoinType.LEFT);
                List<Predicate> salePredicates = new ArrayList<>();
                salePredicates.add(criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("compareAtPrice")),
                        criteriaBuilder.greaterThan(root.get("compareAtPrice"), root.get("price"))
                ));
                salePredicates.add(criteriaBuilder.equal(criteriaBuilder.lower(saleTagJoin.as(String.class)), "sale"));
                salePredicates.add(criteriaBuilder.equal(criteriaBuilder.lower(saleTagJoin.as(String.class)), "last-season"));
                salePredicates.addAll(activePromotionPredicates(
                        activePromotions,
                        root,
                        categoryJoin,
                        collectionJoin,
                        criteriaBuilder
                ));
                predicates.add(criteriaBuilder.or(salePredicates.toArray(Predicate[]::new)));
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

            String searchTerm = normalize(request.search());
            if (!searchTerm.isBlank()) {
                String pattern = "%" + searchTerm + "%";
                var searchTagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(collectionJoin.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(collectionJoin.get("slug")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(searchTagJoin.as(String.class)), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private List<Predicate> activePromotionPredicates(
            List<Promotion> activePromotions,
            Root<Product> root,
            Join<Product, Category> categoryJoin,
            Join<Product, FashionCollection> collectionJoin,
            CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> predicates = new ArrayList<>();

        for (Promotion promotion : activePromotions) {
            List<String> targets = promotion.getTargetValues()
                    .stream()
                    .map(this::normalize)
                    .filter(value -> !value.isBlank())
                    .toList();
            if (targets.isEmpty()) {
                continue;
            }

            PromotionTargetType targetType = promotion.getTargetType();
            if (targetType == PromotionTargetType.SELECTED_PRODUCTS) {
                Set<Long> productIds = parseIds(targets);
                if (!productIds.isEmpty()) {
                    predicates.add(root.get("id").in(productIds));
                }
            } else if (targetType == PromotionTargetType.CATEGORY) {
                predicates.add(targetMatchPredicate(
                        targets,
                        parseIds(targets),
                        categoryJoin,
                        criteriaBuilder
                ));
            } else if (targetType == PromotionTargetType.COLLECTION) {
                predicates.add(targetMatchPredicate(
                        targets,
                        parseIds(targets),
                        collectionJoin,
                        criteriaBuilder
                ));
            } else if (targetType == PromotionTargetType.SEASON) {
                predicates.add(criteriaBuilder.lower(root.get("season")).in(targets));
            } else if (targetType == PromotionTargetType.TAGS) {
                var promotionTagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(criteriaBuilder.lower(promotionTagJoin.as(String.class)).in(targets));
            }
        }

        return predicates;
    }

    private <T> Predicate targetMatchPredicate(
            List<String> targets,
            Set<Long> targetIds,
            Join<Product, T> join,
            CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> targetPredicates = new ArrayList<>();
        if (!targetIds.isEmpty()) {
            targetPredicates.add(join.get("id").in(targetIds));
        }
        targetPredicates.add(criteriaBuilder.lower(join.get("slug")).in(targets));
        targetPredicates.add(criteriaBuilder.lower(join.get("name")).in(targets));
        return criteriaBuilder.or(targetPredicates.toArray(Predicate[]::new));
    }

    private Set<Long> parseIds(List<String> values) {
        Set<Long> ids = new HashSet<>();
        for (String value : values) {
            try {
                ids.add(Long.parseLong(value));
            } catch (NumberFormatException ignored) {
                // Non-numeric targets can still match slugs, names, seasons, or tags.
            }
        }
        return ids;
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "name" : sort) {
            case "price-low", "price-asc" -> Sort.by("price").ascending().and(Sort.by("name").ascending());
            case "price-high", "price-desc" -> Sort.by("price").descending().and(Sort.by("name").ascending());
            case "stock" -> Sort.by("stockQuantity").descending().and(Sort.by("name").ascending());
            case "newest" -> Sort.by("createdAt").descending();
            case "best-selling" -> Sort.by("featured").descending().and(Sort.by("createdAt").descending());
            case "discount" -> Sort.by("compareAtPrice").descending().and(Sort.by("price").ascending());
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

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
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
                category.getImageUrl(),
                category.getSortOrder(),
                category.isActive()
        );
        FashionCollection collection = product.getCollection();
        CollectionResponse collectionResponse = collection == null ? null : new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getSlug(),
                collection.getDescription(),
                collection.getHeroImageUrl(),
                collection.getDisplayImageUrl(),
                collection.getStatus(),
                collection.isFeatured(),
                collection.getStartDate(),
                collection.getEndDate(),
                collection.getSortOrder(),
                0,
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
        PromotionService.DiscountQuote quote = promotionService.quote(product);

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
                List.copyOf(product.getSizes()),
                List.copyOf(product.getColors()),
                product.getMaterial(),
                product.getCareInstructions(),
                product.getSeason(),
                product.getGenderTarget(),
                quote.effectivePrice(),
                quote.discountAmount(),
                quote.discountPercent(),
                product.isFeatured(),
                product.getStatus(),
                product.isActive(),
                categoryResponse,
                collectionResponse
        );
    }
}
