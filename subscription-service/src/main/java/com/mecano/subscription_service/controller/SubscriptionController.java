package com.mecano.subscription_service.controller;

import com.mecano.subscription_service.entity.*;
import com.mecano.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getPlans() {
        return ResponseEntity.ok(service.getAllPlans());
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(
            @RequestParam UUID mechanicId,
            @RequestParam PlanLevel planLevel) {
        String url = service.createCheckoutSession(mechanicId, planLevel);
        return ResponseEntity.ok(Map.of("checkoutUrl", url));
    }

    @GetMapping("/active")
    public ResponseEntity<MechanicSubscription> getActive(
            @RequestParam UUID mechanicId) {
        return ResponseEntity.ok(service.getActiveSubscription(mechanicId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MechanicSubscription>> getHistory(
            @RequestParam UUID mechanicId) {
        return ResponseEntity.ok(service.getHistory(mechanicId));
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<MechanicSubscription> cancel(
            @RequestParam UUID mechanicId) {
        return ResponseEntity.ok(service.cancelSubscription(mechanicId));
    }
}
