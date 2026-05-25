package com.novacart.store.service;

import com.novacart.store.dto.OrderDtos;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.entity.Listing;
import com.novacart.store.entity.ListingStatus;
import com.novacart.store.entity.Offer;
import com.novacart.store.entity.OfferStatus;
import com.novacart.store.entity.OrderStatus;
import com.novacart.store.entity.TradeOrder;
import com.novacart.store.entity.User;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.OfferRepository;
import com.novacart.store.repository.TradeOrderRepository;
import com.novacart.store.security.CurrentUserService;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final TradeOrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final ListingService listingService;
    private final CurrentUserService currentUserService;

    public OrderService(
            TradeOrderRepository orderRepository,
            OfferRepository offerRepository,
            ListingService listingService,
            CurrentUserService currentUserService
    ) {
        this.orderRepository = orderRepository;
        this.offerRepository = offerRepository;
        this.listingService = listingService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public OrderDtos.OrderResponse create(OrderDtos.CreateOrderRequest request) {
        User buyer = currentUserService.requireCurrentUser();
        Listing listing = listingService.requireById(request.listingId());
        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new BusinessRuleException("You cannot buy your own listing.");
        }
        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new BusinessRuleException("This listing has already been sold.");
        }
        if (listing.getStatus() == ListingStatus.REMOVED) {
            throw new BusinessRuleException("This listing is no longer available.");
        }

        BigDecimal price = listing.getPrice();
        if (request.acceptedOfferId() != null) {
            Offer offer = offerRepository.findById(request.acceptedOfferId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));
            if (!offer.getBuyer().getId().equals(buyer.getId())) {
                throw new BusinessRuleException("This offer is not yours.");
            }
            if (!offer.getListing().getId().equals(listing.getId())) {
                throw new BusinessRuleException("Offer does not match listing.");
            }
            if (offer.getStatus() != OfferStatus.ACCEPTED) {
                throw new BusinessRuleException("Only accepted offers can be used to check out.");
            }
            price = offer.getAmount();
        } else {
            // if listing is RESERVED, only the buyer of the accepted offer can buy at list price
            if (listing.getStatus() == ListingStatus.RESERVED) {
                throw new BusinessRuleException("This listing is reserved. Use your accepted offer to check out.");
            }
        }

        BigDecimal shippingFee = listing.getShippingFee() == null ? BigDecimal.ZERO : listing.getShippingFee();
        TradeOrder order = new TradeOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setListing(listing);
        order.setBuyer(buyer);
        order.setSeller(listing.getSeller());
        order.setAgreedPrice(price);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(price.add(shippingFee));
        order.setShippingName(request.shippingName());
        order.setShippingPhone(request.shippingPhone());
        order.setShippingAddress(request.shippingAddress());
        order.setBuyerNote(request.buyerNote());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreatedAt(Instant.now());

        listingService.markReserved(listing);
        return OrderDtos.OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderDtos.OrderResponse pay(Long orderId) {
        User current = currentUserService.requireCurrentUser();
        TradeOrder order = requireById(orderId);
        if (!order.getBuyer().getId().equals(current.getId())) {
            throw new BusinessRuleException("Only the buyer can pay this order.");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Order is not awaiting payment.");
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(Instant.now());
        return OrderDtos.OrderResponse.from(order);
    }

    @Transactional
    public OrderDtos.OrderResponse ship(Long orderId, OrderDtos.ShipOrderRequest request) {
        User current = currentUserService.requireCurrentUser();
        TradeOrder order = requireById(orderId);
        if (!order.getSeller().getId().equals(current.getId())) {
            throw new BusinessRuleException("Only the seller can ship this order.");
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessRuleException("Order must be paid before shipping.");
        }
        order.setCarrier(request.carrier());
        order.setTrackingNumber(request.trackingNumber());
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(Instant.now());
        return OrderDtos.OrderResponse.from(order);
    }

    @Transactional
    public OrderDtos.OrderResponse confirmReceipt(Long orderId) {
        User current = currentUserService.requireCurrentUser();
        TradeOrder order = requireById(orderId);
        if (!order.getBuyer().getId().equals(current.getId())) {
            throw new BusinessRuleException("Only the buyer can confirm receipt.");
        }
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessRuleException("Order has not been shipped yet.");
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setDeliveredAt(Instant.now());
        order.setCompletedAt(Instant.now());
        listingService.markSold(order.getListing());
        return OrderDtos.OrderResponse.from(order);
    }

    @Transactional
    public OrderDtos.OrderResponse cancel(Long orderId, OrderDtos.CancelOrderRequest request) {
        User current = currentUserService.requireCurrentUser();
        TradeOrder order = requireById(orderId);
        boolean isParticipant = order.getBuyer().getId().equals(current.getId())
                || order.getSeller().getId().equals(current.getId());
        if (!isParticipant) {
            throw new BusinessRuleException("You are not a participant in this order.");
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Order cannot be cancelled in its current state.");
        }
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new BusinessRuleException("Shipped orders cannot be cancelled here. Open a dispute instead.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancelReason(request == null ? null : request.reason());
        listingService.releaseReservation(order.getListing());
        return OrderDtos.OrderResponse.from(order);
    }

    public OrderDtos.OrderResponse get(Long orderId) {
        User current = currentUserService.requireCurrentUser();
        TradeOrder order = requireById(orderId);
        if (!order.getBuyer().getId().equals(current.getId()) && !order.getSeller().getId().equals(current.getId())) {
            throw new BusinessRuleException("You cannot view this order.");
        }
        return OrderDtos.OrderResponse.from(order);
    }

    public PageResponse<OrderDtos.OrderResponse> listAsBuyer(String status, int page, int size) {
        User buyer = currentUserService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(60, Math.max(1, size)));
        Page<TradeOrder> result = status == null || status.isBlank()
                ? orderRepository.findByBuyerOrderByCreatedAtDesc(buyer, pageable)
                : orderRepository.findByBuyerAndStatusOrderByCreatedAtDesc(buyer, OrderStatus.valueOf(status.toUpperCase()), pageable);
        return PageResponse.from(result.map(OrderDtos.OrderResponse::from));
    }

    public PageResponse<OrderDtos.OrderResponse> listAsSeller(String status, int page, int size) {
        User seller = currentUserService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(60, Math.max(1, size)));
        Page<TradeOrder> result = status == null || status.isBlank()
                ? orderRepository.findBySellerOrderByCreatedAtDesc(seller, pageable)
                : orderRepository.findBySellerAndStatusOrderByCreatedAtDesc(seller, OrderStatus.valueOf(status.toUpperCase()), pageable);
        return PageResponse.from(result.map(OrderDtos.OrderResponse::from));
    }

    public TradeOrder requireById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }

    private String generateOrderNumber() {
        return "RN" + System.currentTimeMillis() + String.format("%04d", RANDOM.nextInt(10000));
    }
}
