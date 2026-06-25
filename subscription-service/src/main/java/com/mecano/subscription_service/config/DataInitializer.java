package com.mecano.subscription_service.config;

import com.mecano.subscription_service.entity.PlanLevel;
import com.mecano.subscription_service.entity.SubscriptionPlan;
import com.mecano.subscription_service.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SubscriptionPlanRepository planRepo;

    @Override
    public void run(String... args) {
        if (planRepo.count() == 0) {
            planRepo.save(SubscriptionPlan.builder()
                    .level(PlanLevel.BASIC)
                    .price(new BigDecimal("14.99"))
                    .currency("EUR")
                    .priorityScore(1)
                    .description("Visibilité standard dans votre zone")
                    .stripePriceId("price_BASIC_ID_STRIPE")
                    .build());

            planRepo.save(SubscriptionPlan.builder()
                    .level(PlanLevel.SILVER)
                    .price(new BigDecimal("24.99"))
                    .currency("EUR")
                    .priorityScore(2)
                    .description("Priorité moyenne — profil enrichi")
                    .stripePriceId("price_SILVER_ID_STRIPE")
                    .build());

            planRepo.save(SubscriptionPlan.builder()
                    .level(PlanLevel.GOLD)
                    .price(new BigDecimal("49.99"))
                    .currency("EUR")
                    .priorityScore(3)
                    .description("Priorité maximale — profil premium")
                    .stripePriceId("price_GOLD_ID_STRIPE")
                    .build());

            System.out.println("✅ Plans initialisés : BASIC / SILVER / GOLD");
        }
    }
}
