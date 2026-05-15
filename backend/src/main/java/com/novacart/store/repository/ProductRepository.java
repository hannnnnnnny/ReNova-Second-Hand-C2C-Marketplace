package com.novacart.store.repository;

import com.novacart.store.entity.Product;
import com.novacart.store.entity.ProductStatus;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueAndCategoryIdOrderByNameAsc(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueAndStatusOrderByNameAsc(ProductStatus status);

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueAndStatusAndCategoryIdOrderByNameAsc(ProductStatus status, Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByStockQuantityLessThanEqualOrderByStockQuantityAsc(int threshold);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrueAndStatus(Long id, ProductStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByCategoryId(Long categoryId);

    long countByActiveTrue();

    long countByActiveTrueAndStatus(ProductStatus status);

    long countByStockQuantityLessThanEqual(int threshold);
}
