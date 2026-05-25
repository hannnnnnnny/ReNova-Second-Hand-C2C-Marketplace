package com.novacart.store.repository;

import com.novacart.store.entity.Listing;
import com.novacart.store.entity.Offer;
import com.novacart.store.entity.OfferStatus;
import com.novacart.store.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Page<Offer> findByListing_SellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    Page<Offer> findByBuyerOrderByCreatedAtDesc(User buyer, Pageable pageable);

    List<Offer> findByListingOrderByCreatedAtDesc(Listing listing);

    long countByListing_SellerAndStatus(User seller, OfferStatus status);
}
