package com.novacart.store.repository;

import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.TradeOrder;
import com.novacart.store.entity.User;
import java.util.Optional;
import java.util.Collection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    Optional<TradeOrder> findByOrderNumber(String orderNumber);

    Optional<TradeOrder> findByBuyerAndIdempotencyKey(User buyer, String idempotencyKey);

    boolean existsByListingAndStatusIn(com.novacart.store.entity.Listing listing, Collection<OrderStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TradeOrder> findTop100ByStatusAndReservationExpiresAtBeforeOrderByReservationExpiresAtAsc(
            OrderStatus status,
            Instant expiresBefore
    );

    Page<TradeOrder> findByBuyerOrderByCreatedAtDesc(User buyer, Pageable pageable);

    Page<TradeOrder> findBySellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    Page<TradeOrder> findByBuyerAndStatusOrderByCreatedAtDesc(User buyer, OrderStatus status, Pageable pageable);

    Page<TradeOrder> findBySellerAndStatusOrderByCreatedAtDesc(User seller, OrderStatus status, Pageable pageable);

    Optional<TradeOrder> findByIdAndBuyer(Long id, User buyer);

    Optional<TradeOrder> findByIdAndSeller(Long id, User seller);

    @Query("""
            SELECT o FROM TradeOrder o
            WHERE o.id = :id AND (o.buyer = :participant OR o.seller = :participant)
            """)
    Optional<TradeOrder> findByIdAndParticipant(@Param("id") Long id, @Param("participant") User participant);
}
