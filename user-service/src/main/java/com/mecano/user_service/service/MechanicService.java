package com.mecano.user_service.service;

import com.mecano.user_service.dto.MechanicRequest;
import com.mecano.user_service.dto.MechanicResponse;
import com.mecano.user_service.entity.Mechanic;
import com.mecano.user_service.entity.SubscriptionLevel;
import com.mecano.user_service.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository repo;

    @Transactional
    public MechanicResponse create(MechanicRequest req) {
        if (repo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email déjà utilisé");

        Mechanic m = repo.save(Mechanic.builder()
                .authUserId(req.getAuthUserId())
                .userId(req.getUserId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .garageName(req.getGarageName())
                .garageAddress(req.getGarageAddress())
                .specialities(req.getSpecialities())
                .justificationDocument(req.getJustificationDocument())
                .subscriptionLevel(SubscriptionLevel.BASIC)
                .available(true)
                .build());
        return toResponse(m);
    }

    public MechanicResponse getById(UUID id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Mécanicien introuvable : " + id)));
    }

    public MechanicResponse getByAuthUserId(UUID authUserId) {
        return toResponse(repo.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Mécanicien introuvable")));
    }

    public List<MechanicResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<MechanicResponse> getAvailable() {
        return repo.findByAvailableTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MechanicResponse update(UUID id, MechanicRequest req) {
        Mechanic m = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Mécanicien introuvable"));
        m.setFirstName(req.getFirstName());
        m.setLastName(req.getLastName());
        m.setPhone(req.getPhone());
        m.setGarageName(req.getGarageName());
        m.setGarageAddress(req.getGarageAddress());
        m.setSpecialities(req.getSpecialities());
        return toResponse(repo.save(m));
    }

    @Transactional
    public MechanicResponse updateSubscription(UUID id, SubscriptionLevel level) {
        Mechanic m = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Mécanicien introuvable"));
        m.setSubscriptionLevel(level);
        return toResponse(repo.save(m));
    }

    @Transactional
    public MechanicResponse toggleAvailability(UUID id) {
        Mechanic m = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Mécanicien introuvable"));
        m.setAvailable(!m.isAvailable());
        return toResponse(repo.save(m));
    }

    @Transactional
    public void delete(UUID id) { repo.deleteById(id); }

    private MechanicResponse toResponse(Mechanic m) {
        return MechanicResponse.builder()
                .id(m.getId())
                .authUserId(m.getAuthUserId())
                .firstName(m.getFirstName())
                .lastName(m.getLastName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .garageName(m.getGarageName())
                .garageAddress(m.getGarageAddress())
                .specialities(m.getSpecialities())
                .subscriptionLevel(m.getSubscriptionLevel())
                .priorityScore(m.getSubscriptionLevel().getPriorityScore())
                .available(m.isAvailable())
                .build();
    }
}
