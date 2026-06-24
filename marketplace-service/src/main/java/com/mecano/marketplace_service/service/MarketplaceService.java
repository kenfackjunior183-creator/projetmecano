package com.mecano.marketplace_service.service;

import com.mecano.marketplace_service.dto.CreateListingRequest;
import com.mecano.marketplace_service.dto.CreateOfferRequest;
import com.mecano.marketplace_service.entity.Offer;
import com.mecano.marketplace_service.entity.PartListing;
import com.mecano.marketplace_service.repository.OfferRepository;
import com.mecano.marketplace_service.repository.PartListingRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final PartListingRepository listingRepository;
    private final OfferRepository offerRepository;

    // ── Listings ────────────────────────────────────────────────────────────

    @Transactional
    public PartListing createListing(String sellerId, CreateListingRequest request) {
        PartListing listing = PartListing.builder()
                .sellerId(sellerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .brand(request.getBrand())
                .model(request.getModel())
                .price(request.getPrice())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .condition(request.getCondition() != null ? request.getCondition() : "USED")
                .negotiable(request.getNegotiable() != null ? request.getNegotiable() : true)
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .build();
        return listingRepository.save(listing);
    }

    public PartListing getListingById(String listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + listingId));
    }

    public List<PartListing> getActiveListings() {
        return listingRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
    }

    public List<PartListing> searchListings(String keyword, String category, String brand,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             String location, String condition) {
        Specification<PartListing> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"));
            }
            if (condition != null && !condition.isBlank()) {
                predicates.add(cb.equal(root.get("condition"), condition));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return listingRepository.findAll(spec);
    }

    public List<PartListing> getMyListings(String sellerId) {
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @Transactional
    public PartListing updateListing(String listingId, String sellerId, CreateListingRequest request) {
        PartListing listing = getListingById(listingId);
        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Only the seller can update this listing");
        }
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory());
        listing.setBrand(request.getBrand());
        listing.setModel(request.getModel());
        listing.setPrice(request.getPrice());
        listing.setQuantity(request.getQuantity() != null ? request.getQuantity() : listing.getQuantity());
        listing.setCondition(request.getCondition() != null ? request.getCondition() : listing.getCondition());
        listing.setNegotiable(request.getNegotiable() != null ? request.getNegotiable() : listing.getNegotiable());
        listing.setImageUrl(request.getImageUrl());
        listing.setLocation(request.getLocation());
        return listingRepository.save(listing);
    }

    @Transactional
    public void deleteListing(String listingId, String sellerId) {
        PartListing listing = getListingById(listingId);
        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Only the seller can delete this listing");
        }
        listing.setStatus("DELETED");
        listingRepository.save(listing);
    }

    @Transactional
    public PartListing markAsSold(String listingId, String sellerId) {
        PartListing listing = getListingById(listingId);
        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Only the seller can update this listing");
        }
        listing.setStatus("SOLD");
        return listingRepository.save(listing);
    }

    // ── Offers ──────────────────────────────────────────────────────────────

    @Transactional
    public Offer makeOffer(String buyerId, CreateOfferRequest request) {
        PartListing listing = getListingById(request.getListingId());
        if ("SOLD".equals(listing.getStatus()) || "DELETED".equals(listing.getStatus())) {
            throw new RuntimeException("Cannot make an offer on an inactive listing");
        }
        if (listing.getSellerId().equals(buyerId)) {
            throw new RuntimeException("You cannot make an offer on your own listing");
        }
        Offer offer = Offer.builder()
                .listingId(request.getListingId())
                .buyerId(buyerId)
                .offeredPrice(request.getOfferedPrice())
                .message(request.getMessage())
                .build();
        return offerRepository.save(offer);
    }

    public List<Offer> getOffersForListing(String listingId, String sellerId) {
        PartListing listing = getListingById(listingId);
        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Only the seller can view offers on their listing");
        }
        return offerRepository.findByListingIdOrderByCreatedAtDesc(listingId);
    }

    public List<Offer> getMyOffers(String buyerId) {
        return offerRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    @Transactional
    public Offer respondToOffer(String offerId, String sellerId, String status) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        PartListing listing = getListingById(offer.getListingId());
        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Only the listing seller can respond to this offer");
        }
        if (!"ACCEPTED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("Status must be ACCEPTED or REJECTED");
        }
        offer.setStatus(status);
        offer.setRespondedAt(LocalDateTime.now());

        if ("ACCEPTED".equals(status)) {
            listing.setStatus("SOLD");
            listingRepository.save(listing);
            // Reject all other pending offers on this listing
            List<Offer> otherOffers = offerRepository.findByListingIdOrderByCreatedAtDesc(offer.getListingId());
            for (Offer other : otherOffers) {
                if (!other.getId().equals(offerId) && "PENDING".equals(other.getStatus())) {
                    other.setStatus("REJECTED");
                    other.setRespondedAt(LocalDateTime.now());
                    offerRepository.save(other);
                }
            }
        }
        return offerRepository.save(offer);
    }
}