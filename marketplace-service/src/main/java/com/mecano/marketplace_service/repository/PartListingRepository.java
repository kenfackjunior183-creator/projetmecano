package com.mecano.marketplace_service.repository;

import com.mecano.marketplace_service.entity.PartListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartListingRepository extends JpaRepository<PartListing, String>,
        JpaSpecificationExecutor<PartListing> {

    List<PartListing> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    List<PartListing> findByStatusOrderByCreatedAtDesc(String status);

    List<PartListing> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status);

    List<PartListing> findByBrandIgnoreCaseAndStatusOrderByCreatedAtDesc(String brand, String status);

    List<PartListing> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
            String titleKeyword, String descKeyword, String status);

    List<PartListing> findByLocationContainingIgnoreCaseAndStatus(String location, String status);
}