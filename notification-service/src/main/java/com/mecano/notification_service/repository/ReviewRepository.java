package com.mecano.notification_service.repository;

import com.mecano.notification_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByMechanicId(UUID mechanicId);
    List<Review> findByAutomobilistId(UUID automobilistId);
    Optional<Review> findByRepairRequestId(UUID repairRequestId);
}