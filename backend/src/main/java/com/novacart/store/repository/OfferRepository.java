package com.novacart.store.repository;

import com.novacart.store.entity.Listing;
import com.novacart.store.entity.Offer;
import com.novacart.store.entity.OfferStatus;
import com.novacart.store.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Page<Offer> findByListing_SellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    Page<Offer> findByBuyerOrderByCreatedAtDesc(User buyer, Pageable pageable);

    List<Offer> findByListingOrderByCreatedAtDesc(Listing listing);

    long countByListing_SellerAndStatus(User seller, OfferStatus status);

    Optional<Offer> findByIdAndListing_Seller(Long id, User seller);

    Optional<Offer> findByIdAndBuyer(Long id, User buyer);

    @Query("""
            SELECT o FROM Offer o
            WHERE o.id = :id
              AND (o.buyer = :participant OR o.listing.seller = :participant)
            """)
    Optional<Offer> findByIdAndParticipant(@Param("id") Long id, @Param("participant") User participant);

    @Query("""
            SELECT o FROM Offer o
            WHERE o.id = :id
              AND ((o.fromSeller = true AND o.listing.seller = :actor)
                   OR (o.fromSeller = false AND o.buyer = :actor))
            """)
    Optional<Offer> findAuthoredBy(@Param("id") Long id, @Param("actor") User actor);
}
