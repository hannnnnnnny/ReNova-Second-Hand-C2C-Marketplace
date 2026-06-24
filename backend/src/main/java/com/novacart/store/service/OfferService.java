package com.novacart.store.service;

import com.novacart.store.dto.OfferDtos;
import com.novacart.store.dto.PageResponse;
import com.novacart.store.entity.Listing;
import com.novacart.store.entity.ListingStatus;
import com.novacart.store.entity.Offer;
import com.novacart.store.entity.OfferStatus;
import com.novacart.store.entity.User;
import com.novacart.store.exception.BusinessRuleException;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.OfferRepository;
import com.novacart.store.security.CurrentUserService;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final ListingService listingService;
    private final CurrentUserService currentUserService;

    public OfferService(OfferRepository offerRepository, ListingService listingService, CurrentUserService currentUserService) {
        this.offerRepository = offerRepository;
        this.listingService = listingService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public OfferDtos.OfferResponse create(OfferDtos.OfferCreateRequest request) {
        User buyer = currentUserService.requireCurrentUser();
        Listing listing = listingService.requireById(request.listingId());
        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new BusinessRuleException("You cannot make an offer on your own listing.");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessRuleException("This listing is no longer available for offers.");
        }
        if (!listing.isNegotiable()) {
            throw new BusinessRuleException("Seller does not accept offers on this listing.");
        }
        Offer offer = new Offer();
        offer.setListing(listing);
        offer.setBuyer(buyer);
        offer.setAmount(request.amount());
        offer.setMessage(request.message());
        offer.setFromSeller(false);
        offer.setStatus(OfferStatus.PENDING);
        offer.setCreatedAt(Instant.now());
        return OfferDtos.OfferResponse.from(offerRepository.save(offer));
    }

    @Transactional
    public OfferDtos.OfferResponse accept(Long offerId) {
        User current = currentUserService.requireCurrentUser();
        Offer offer = requireForSeller(offerId, current);
        if (offer.isFromSeller()) {
            throw new BusinessRuleException("A seller cannot accept their own counter-offer.");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessRuleException("This offer is not pending.");
        }
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setRespondedAt(Instant.now());
        listingService.markReserved(offer.getListing());
        return OfferDtos.OfferResponse.from(offer);
    }

    @Transactional
    public OfferDtos.OfferResponse reject(Long offerId) {
        User current = currentUserService.requireCurrentUser();
        Offer offer = requireForSeller(offerId, current);
        if (offer.isFromSeller()) {
            throw new BusinessRuleException("Withdraw your own counter-offer instead.");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessRuleException("This offer is not pending.");
        }
        offer.setStatus(OfferStatus.REJECTED);
        offer.setRespondedAt(Instant.now());
        return OfferDtos.OfferResponse.from(offer);
    }

    @Transactional
    public OfferDtos.OfferResponse counter(Long offerId, OfferDtos.OfferCounterRequest request) {
        User current = currentUserService.requireCurrentUser();
        Offer original = requireForSeller(offerId, current);
        if (original.isFromSeller()) {
            throw new BusinessRuleException("Withdraw your existing counter-offer before sending another.");
        }
        if (original.getStatus() != OfferStatus.PENDING) {
            throw new BusinessRuleException("This offer is not pending.");
        }
        original.setStatus(OfferStatus.COUNTERED);
        original.setRespondedAt(Instant.now());

        Offer counter = new Offer();
        counter.setListing(original.getListing());
        counter.setBuyer(original.getBuyer());
        counter.setParentOffer(original);
        counter.setAmount(request.amount());
        counter.setMessage(request.message());
        counter.setFromSeller(true);
        counter.setStatus(OfferStatus.PENDING);
        counter.setCreatedAt(Instant.now());
        return OfferDtos.OfferResponse.from(offerRepository.save(counter));
    }

    @Transactional
    public OfferDtos.OfferResponse withdraw(Long offerId) {
        User current = currentUserService.requireCurrentUser();
        Offer offer = offerRepository.findAuthoredBy(offerId, current)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessRuleException("Only pending offers can be withdrawn.");
        }
        offer.setStatus(OfferStatus.WITHDRAWN);
        offer.setRespondedAt(Instant.now());
        return OfferDtos.OfferResponse.from(offer);
    }

    @Transactional
    public OfferDtos.OfferResponse acceptCounter(Long offerId) {
        User current = currentUserService.requireCurrentUser();
        Offer offer = offerRepository.findByIdAndBuyer(offerId, current)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));
        if (!offer.isFromSeller()) {
            throw new BusinessRuleException("Only counter-offers from the seller can be accepted here.");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessRuleException("This counter-offer is no longer pending.");
        }
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setRespondedAt(Instant.now());
        listingService.markReserved(offer.getListing());
        return OfferDtos.OfferResponse.from(offer);
    }

    @Transactional(readOnly = true)
    public PageResponse<OfferDtos.OfferResponse> receivedOffers(int page, int size) {
        User seller = currentUserService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(60, Math.max(1, size)));
        Page<Offer> result = offerRepository.findByListing_SellerOrderByCreatedAtDesc(seller, pageable);
        return PageResponse.from(result.map(OfferDtos.OfferResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<OfferDtos.OfferResponse> sentOffers(int page, int size) {
        User buyer = currentUserService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(60, Math.max(1, size)));
        Page<Offer> result = offerRepository.findByBuyerOrderByCreatedAtDesc(buyer, pageable);
        return PageResponse.from(result.map(OfferDtos.OfferResponse::from));
    }

    @Transactional(readOnly = true)
    public OfferDtos.OfferResponse get(Long offerId) {
        User participant = currentUserService.requireCurrentUser();
        Offer offer = offerRepository.findByIdAndParticipant(offerId, participant)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));
        return OfferDtos.OfferResponse.from(offer);
    }

    private Offer requireForSeller(Long id, User seller) {
        return offerRepository.findByIdAndListing_Seller(id, seller)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));
    }
}
