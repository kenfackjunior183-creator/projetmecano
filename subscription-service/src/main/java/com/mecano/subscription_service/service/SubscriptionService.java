package com.mecano.subscription_service.service;

import com.mecano.subscription_service.entity.*;
import com.mecano.subscription_service.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepo;
    private final MechanicSubscriptionRepository subRepo;

    @Value("${application.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${application.stripe.success-url}")
    private String successUrl;

    @Value("${application.stripe.cancel-url}")
    private String cancelUrl;

    public List<SubscriptionPlan> getAllPlans() {
        return planRepo.findAll();
    }

    public SubscriptionPlan getPlanByLevel(PlanLevel level) {
        return planRepo.findByLevel(level)
                .orElseThrow(() -> new RuntimeException("Plan introuvable : " + level));
    }

    @Transactional
    public String createCheckoutSession(UUID mechanicId, PlanLevel planLevel) {
        SubscriptionPlan plan = getPlanByLevel(planLevel);

        if (stripeSecretKey == null || stripeSecretKey.isBlank()
                || stripeSecretKey.contains("REMPLACE_PAR_TA_CLE")) {
            throw new RuntimeException("La clé Stripe n'est pas configurée. Définissez STRIPE_SECRET_KEY.");
        }

        if (plan.getStripePriceId() == null || plan.getStripePriceId().contains("ID_STRIPE")) {
            throw new RuntimeException("Le prix Stripe n'est pas configuré pour le plan " + planLevel);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(plan.getStripePriceId())
                        .setQuantity(1L)
                        .build())
                .putMetadata("mechanicId", mechanicId.toString())
                .putMetadata("planLevel", planLevel.name())
                .build();

        try {
            Session session = Session.create(params);
            subRepo.save(MechanicSubscription.builder()
                    .mechanicId(mechanicId)
                    .plan(plan)
                    .status(SubscriptionStatus.PENDING)
                    .stripeSessionId(session.getId())
                    .build());
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("Erreur Stripe lors de la création de la session de paiement : "
                    + e.getMessage(), e);
        }
    }

    @Transactional
    public MechanicSubscription activateSubscription(String stripeSessionId,
                                                      String stripeSubscriptionId) {
        MechanicSubscription sub = subRepo.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStripeSubscriptionId(stripeSubscriptionId);
        sub.setStartDate(LocalDateTime.now());
        sub.setEndDate(LocalDateTime.now().plusMonths(1));
        sub.setPaymentReference("stripe_" + stripeSubscriptionId);
        return subRepo.save(sub);
    }

    @Transactional
    public MechanicSubscription cancelSubscription(UUID mechanicId) {
        MechanicSubscription sub = subRepo
                .findByMechanicIdAndStatus(mechanicId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement actif"));

        if (sub.getStripeSubscriptionId() != null) {
            try {
                Subscription s = Subscription.retrieve(sub.getStripeSubscriptionId());
                s.cancel();
            } catch (StripeException e) {
                throw new RuntimeException("Erreur Stripe lors de l'annulation de l'abonnement : "
                        + e.getMessage(), e);
            }
        }

        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setEndDate(LocalDateTime.now());
        return subRepo.save(sub);
    }

    public MechanicSubscription getActiveSubscription(UUID mechanicId) {
        return subRepo.findByMechanicIdAndStatus(mechanicId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement actif"));
    }

    public List<MechanicSubscription> getHistory(UUID mechanicId) {
        return subRepo.findByMechanicId(mechanicId);
    }
}
