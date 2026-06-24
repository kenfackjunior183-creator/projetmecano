package com.mecano.marketplace_service.controller;

import com.mecano.marketplace_service.dto.CreateListingRequest;
import com.mecano.marketplace_service.dto.CreateOfferRequest;
import com.mecano.marketplace_service.entity.Offer;
import com.mecano.marketplace_service.entity.PartListing;
import com.mecano.marketplace_service.service.MarketplaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    // ── Public endpoints ────────────────────────────────────────────────────

    @GetMapping("/listings/public")
    public ResponseEntity<List<PartListing>> getActiveListings() {
        return ResponseEntity.ok(marketplaceService.getActiveListings());
    }

    @GetMapping("/listings/public/{id}")
    public ResponseEntity<PartListing> getListing(@PathVariable String id) {
        return ResponseEntity.ok(marketplaceService.getListingById(id));
    }

    @GetMapping("/listings/public/search")
    public ResponseEntity<List<PartListing>> searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String condition) {
        return ResponseEntity.ok(
                marketplaceService.searchListings(keyword, category, brand,
                        minPrice, maxPrice, location, condition));
    }

    // ── Authenticated listing endpoints ─────────────────────────────────────

    @PostMapping("/listings")
    public ResponseEntity<PartListing> createListing(
            Authentication auth,
            @Valid @RequestBody CreateListingRequest request) {
        return ResponseEntity.ok(marketplaceService.createListing(auth.getName(), request));
    }

    @GetMapping("/listings/me")
    public ResponseEntity<List<PartListing>> getMyListings(Authentication auth) {
        return ResponseEntity.ok(marketplaceService.getMyListings(auth.getName()));
    }

    @PutMapping("/listings/{id}")
    public ResponseEntity<PartListing> updateListing(
            @PathVariable String id,
            Authentication auth,
            @Valid @RequestBody CreateListingRequest request) {
        return ResponseEntity.ok(marketplaceService.updateListing(id, auth.getName(), request));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> deleteListing(
            @PathVariable String id,
            Authentication auth) {
        marketplaceService.deleteListing(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/listings/{id}/sold")
    public ResponseEntity<PartListing> markAsSold(
            @PathVariable String id,
            Authentication auth) {
        return ResponseEntity.ok(marketplaceService.markAsSold(id, auth.getName()));
    }

    // ── Offer endpoints ─────────────────────────────────────────────────────

    @PostMapping("/offers")
    public ResponseEntity<Offer> makeOffer(
            Authentication auth,
            @Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.ok(marketplaceService.makeOffer(auth.getName(), request));
    }

    @GetMapping("/offers/me")
    public ResponseEntity<List<Offer>> getMyOffers(Authentication auth) {
        return ResponseEntity.ok(marketplaceService.getMyOffers(auth.getName()));
    }

    @GetMapping("/offers/listing/{listingId}")
    public ResponseEntity<List<Offer>> getOffersForListing(
            @PathVariable String listingId,
            Authentication auth) {
        return ResponseEntity.ok(
                marketplaceService.getOffersForListing(listingId, auth.getName()));
    }

    @PatchMapping("/offers/{offerId}/respond")
    public ResponseEntity<Offer> respondToOffer(
            @PathVariable String offerId,
            Authentication auth,
            @RequestParam String status) {
        return ResponseEntity.ok(
                marketplaceService.respondToOffer(offerId, auth.getName(), status));
    }
}