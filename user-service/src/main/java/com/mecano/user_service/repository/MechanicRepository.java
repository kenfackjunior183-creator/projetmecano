package com.mecano.user_service.repository;

import com.mecano.user_service.entity.Mechanic;
import com.mecano.user_service.entity.SubscriptionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, UUID> {
    Optional<Mechanic> findByEmail(String email);
    Optional<Mechanic> findByAuthUserId(UUID authUserId);
    List<Mechanic> findBySubscriptionLevel(SubscriptionLevel level);
    List<Mechanic> findByAvailableTrue();
    boolean existsByEmail(String email);
}