package com.novacart.store.repository;

import com.novacart.store.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueAndCategoryIdOrderByNameAsc(Long categoryId);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrue(Long id);

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
