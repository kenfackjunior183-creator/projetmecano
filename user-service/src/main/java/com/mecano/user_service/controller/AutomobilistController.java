package com.mecano.user_service.controller;

import com.mecano.user_service.dto.AutomobilistRequest;
import com.mecano.user_service.entity.Automobilist;
import com.mecano.user_service.service.AutomobilistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/automobilists")
@RequiredArgsConstructor
public class AutomobilistController {

    private final AutomobilistService service;

    @PostMapping
    public ResponseEntity<Automobilist> create(@Valid @RequestBody AutomobilistRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<Automobilist>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Automobilist> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/auth/{authUserId}")
    public ResponseEntity<Automobilist> getByAuthUserId(@PathVariable UUID authUserId) {
        return ResponseEntity.ok(service.getByAuthUserId(authUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Automobilist> update(@PathVariable UUID id,
                                               @Valid @RequestBody AutomobilistRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


