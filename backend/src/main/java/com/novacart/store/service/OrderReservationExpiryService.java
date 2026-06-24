package com.novacart.store.service;

import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.TradeOrder;
import com.novacart.store.repository.TradeOrderRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderReservationExpiryService {

    private static final String EXPIRY_REASON = "Payment window expired.";

    private final TradeOrderRepository orderRepository;
    private final ListingService listingService;

    public OrderReservationExpiryService(
            TradeOrderRepository orderRepository,
            ListingService listingService
    ) {
        this.orderRepository = orderRepository;
        this.listingService = listingService;
    }

    @Scheduled(fixedDelayString = "${novacart.orders.expiration-scan-ms}")
    @Transactional
    public void expireScheduledBatch() {
        expireBatch(Instant.now());
    }

    @Transactional
    public int expireBatch(Instant now) {
        List<TradeOrder> expired = orderRepository
                .findTop100ByStatusAndReservationExpiresAtBeforeOrderByReservationExpiresAtAsc(
                        OrderStatus.PENDING_PAYMENT,
                        now
                );
        for (TradeOrder order : expired) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(now);
            order.setCancelReason(EXPIRY_REASON);
            listingService.releaseReservation(order.getListing());
        }
        return expired.size();
    }
}
