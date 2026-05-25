package com.novacart.store.repository;

import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.TradeOrder;
import com.novacart.store.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    Optional<TradeOrder> findByOrderNumber(String orderNumber);

    Page<TradeOrder> findByBuyerOrderByCreatedAtDesc(User buyer, Pageable pageable);

    Page<TradeOrder> findBySellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    Page<TradeOrder> findByBuyerAndStatusOrderByCreatedAtDesc(User buyer, OrderStatus status, Pageable pageable);

    Page<TradeOrder> findBySellerAndStatusOrderByCreatedAtDesc(User seller, OrderStatus status, Pageable pageable);
}
