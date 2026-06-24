package com.mecano.subscription_service.repository;

import com.mecano.subscription_service.entity.MechanicSubscription;
import com.mecano.subscription_service.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MechanicSubscriptionRepository
        extends JpaRepository<MechanicSubscription, UUID> {

    Optional<MechanicSubscription> findByMechanicIdAndStatus(
            UUID mechanicId, SubscriptionStatus status);

    List<MechanicSubscription> findByMechanicId(UUID mechanicId);

    Optional<MechanicSubscription> findByStripeSessionId(String sessionId);
}
