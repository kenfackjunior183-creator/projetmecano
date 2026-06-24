package com.mecano.user_service.service;

import com.mecano.user_service.dto.AutomobilistRequest;
import com.mecano.user_service.entity.Automobilist;
import com.mecano.user_service.repository.AutomobilistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutomobilistService {

    private final AutomobilistRepository repo;

    @Transactional
    public Automobilist create(AutomobilistRequest req) {
        if (repo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email déjà utilisé");

        return repo.save(Automobilist.builder()
                .authUserId(req.getAuthUserId())
                .userId(req.getUserId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .vehicleBrand(req.getVehicleBrand())
                .vehicleModel(req.getVehicleModel())
                .vehiclePlate(req.getVehiclePlate())
                .drivingLicenseDocument(req.getDrivingLicenseDocument())
                .build());
    }

    public Automobilist getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Automobiliste introuvable : " + id));
    }

    public Automobilist getByAuthUserId(UUID authUserId) {
        return repo.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Automobiliste introuvable"));
    }

    public List<Automobilist> getAll() { return repo.findAll(); }

    @Transactional
    public Automobilist update(UUID id, AutomobilistRequest req) {
        Automobilist a = getById(id);
        a.setFirstName(req.getFirstName());
        a.setLastName(req.getLastName());
        a.setPhone(req.getPhone());
        a.setVehicleBrand(req.getVehicleBrand());
        a.setVehicleModel(req.getVehicleModel());
        a.setVehiclePlate(req.getVehiclePlate());
        return repo.save(a);
    }

    @Transactional
    public void delete(UUID id) { repo.deleteById(id); }
}
