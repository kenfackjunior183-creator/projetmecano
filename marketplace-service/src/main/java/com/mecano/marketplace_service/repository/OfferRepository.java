package com.mecano.marketplace_service.repository;

import com.mecano.marketplace_service.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, String> {

    List<Offer> findByListingIdOrderByCreatedAtDesc(String listingId);

    List<Offer> findByBuyerIdOrderByCreatedAtDesc(String buyerId);

    List<Offer> findByListingIdAndBuyerIdOrderByCreatedAtDesc(String listingId, String buyerId);

    List<Offer> findByStatusOrderByCreatedAtDesc(String status);

    boolean existsByListingIdAndBuyerIdAndStatus(String listingId, String buyerId, String status);
}