package com.mecano.notification_service.controller;

import com.mecano.notification_service.dto.*;
import com.mecano.notification_service.entity.Review;
import com.mecano.notification_service.service.NotificationService;
import com.mecano.notification_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ReviewService reviewService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service UP ✅");
    }

    // ── Gestion des avis (reviews) ─────────────────────────────
    @PostMapping("/reviews")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(req));
    }

    @GetMapping("/reviews/mechanic/{mechanicId}")
    public ResponseEntity<List<Review>> getReviewsByMechanic(@PathVariable UUID mechanicId) {
        return ResponseEntity.ok(reviewService.getByMechanicId(mechanicId));
    }

    @GetMapping("/reviews/automobilist/{automobilistId}")
    public ResponseEntity<List<Review>> getReviewsByAutomobilist(@PathVariable UUID automobilistId) {
        return ResponseEntity.ok(reviewService.getByAutomobilistId(automobilistId));
    }

    @GetMapping("/reviews/repair/{repairRequestId}")
    public ResponseEntity<Review> getReviewByRepair(@PathVariable UUID repairRequestId) {
        return reviewService.getByRepairRequestId(repairRequestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
