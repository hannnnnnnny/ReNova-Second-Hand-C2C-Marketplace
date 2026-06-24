package com.novacart.store.repository;

import com.novacart.store.entity.Listing;
import com.novacart.store.entity.ListingStatus;
import com.novacart.store.entity.User;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.id = :id")
    Optional<Listing> findByIdForUpdate(@Param("id") Long id);

    Page<Listing> findBySellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    Page<Listing> findBySellerAndStatusOrderByCreatedAtDesc(User seller, ListingStatus status, Pageable pageable);

    long countBySellerAndStatus(User seller, ListingStatus status);

    @Query("""
            SELECT l FROM Listing l
            WHERE (:status IS NULL OR l.status = :status)
              AND (:categoryId IS NULL OR l.category.id = :categoryId)
              AND (:minPrice IS NULL OR l.price >= :minPrice)
              AND (:maxPrice IS NULL OR l.price <= :maxPrice)
              AND (:condition IS NULL OR l.condition = :condition)
              AND (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR LOWER(l.location) LIKE LOWER(CONCAT('%', :location, '%')))
            """)
    Page<Listing> search(
            @Param("status") ListingStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("condition") com.novacart.store.entity.ListingCondition condition,
            @Param("keyword") String keyword,
            @Param("location") String location,
            Pageable pageable
    );
}
