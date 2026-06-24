package com.mecano.user_service.controller;

import com.mecano.user_service.dto.MechanicRequest;
import com.mecano.user_service.dto.MechanicResponse;
import com.mecano.user_service.entity.SubscriptionLevel;
import com.mecano.user_service.service.MechanicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService service;

    @PostMapping
    public ResponseEntity<MechanicResponse> create(@Valid @RequestBody MechanicRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<MechanicResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<MechanicResponse>> getAvailable() {
        return ResponseEntity.ok(service.getAvailable());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MechanicResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/auth/{authUserId}")
    public ResponseEntity<MechanicResponse> getByAuthUserId(@PathVariable UUID authUserId) {
        return ResponseEntity.ok(service.getByAuthUserId(authUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MechanicResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody MechanicRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PatchMapping("/{id}/subscription")
    public ResponseEntity<MechanicResponse> updateSubscription(
            @PathVariable UUID id,
            @RequestParam SubscriptionLevel level) {
        return ResponseEntity.ok(service.updateSubscription(id, level));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<MechanicResponse> toggleAvailability(@PathVariable UUID id) {
        return ResponseEntity.ok(service.toggleAvailability(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
