package com.mecano.subscription_service.repository;

import com.mecano.subscription_service.entity.PlanLevel;
import com.mecano.subscription_service.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByLevel(PlanLevel level);
}
