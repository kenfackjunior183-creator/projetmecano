package com.mecano.subscription_service.controller;

import com.mecano.subscription_service.service.SubscriptionService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions/webhook")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final SubscriptionService subscriptionService;

    @Value("${application.stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Webhook signature invalide : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Signature invalide");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                EventDataObjectDeserializer d = event.getDataObjectDeserializer();
                if (d.getObject().isPresent()) {
                    Session session = (Session) d.getObject().get();
                    try {
                        subscriptionService.activateSubscription(
                                session.getId(),
                                session.getSubscription());
                        log.info("✅ Abonnement activé : {}", session.getId());
                    } catch (Exception e) {
                        log.error("Erreur activation : {}", e.getMessage());
                    }
                }
            }
            case "customer.subscription.deleted" ->
                log.info("Abonnement supprimé : {}", event.getId());
            default ->
                log.info("Événement ignoré : {}", event.getType());
        }
        return ResponseEntity.ok("OK");
    }
}
