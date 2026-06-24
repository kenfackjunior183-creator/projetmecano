package com.mecano.repair_service.controller;

import com.mecano.repair_service.dto.RepairRequestedEvent;
import com.mecano.repair_service.entity.RepairRequest;
import com.mecano.repair_service.entity.RequestStatus;
import com.mecano.repair_service.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService service;

    // ── Demandes de dépannage ───────────────────────────────────
    @PostMapping("/requests")
    public ResponseEntity<RepairRequest> createRequest(
            @RequestParam UUID automobilistId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createRepairRequest(automobilistId, latitude, longitude, description));
    }

    @PatchMapping("/requests/{id}/assign")
    public ResponseEntity<RepairRequest> assignMechanic(
            @PathVariable UUID id,
            @RequestParam UUID mechanicId) {
        return ResponseEntity.ok(service.assignMechanic(id, mechanicId));
    }

    @PatchMapping("/requests/{id}/status")
    public ResponseEntity<RepairRequest> updateStatus(
            @PathVariable UUID id,
            @RequestParam RequestStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @GetMapping("/requests/automobilist/{id}")
    public ResponseEntity<List<RepairRequest>> getByAutomobilist(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getByAutomobilist(id));
    }

    @GetMapping("/requests/mechanic/{id}")
    public ResponseEntity<List<RepairRequest>> getByMechanic(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getByMechanic(id));
    }

    @GetMapping("/requests/status/{status}")
    public ResponseEntity<List<RepairRequest>> getByStatus(@PathVariable RequestStatus status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    // ── Publication événement de demande de dépannage ──────────
    @PostMapping("/publish/repair-requested")
    public ResponseEntity<String> publishRepairRequested(
            @RequestBody RepairRequestedEvent event) {
        service.publishRepairRequestedEvent(event);
        return ResponseEntity.ok("Event publié → queue.repair.requested");
    }
}