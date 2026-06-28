package com.mecano.geolocation_service.service;

import com.mecano.geolocation_service.dto.*;
import com.mecano.geolocation_service.entity.ClientLocation;
import com.mecano.geolocation_service.entity.MechanicLocation;
import com.mecano.geolocation_service.repository.ClientLocationRepository;
import com.mecano.geolocation_service.repository.MechanicLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeolocationService {

    private final MechanicLocationRepository locationRepo;
    private final ClientLocationRepository clientLocationRepo;
    private final HaversineService haversine;

    // ── Localisation mécanicien ─────────────────────────────────
    @Transactional
    public MechanicLocation saveLocation(LocationRequest req) {
        MechanicLocation loc = locationRepo.findByMechanicId(req.getMechanicId())
                .orElse(MechanicLocation.builder()
                        .mechanicId(req.getMechanicId())
                        .build());
                        
        loc.setLatitude(req.getLatitude());
        loc.setLongitude(req.getLongitude());
        loc.setAddress(req.getAddress());
        loc.setCity(req.getCity());
        loc.setInterventionRadiusKm(req.getInterventionRadiusKm());
        loc.setSubscriptionLevel(req.getSubscriptionLevel());
        loc.setActive(true);
        return locationRepo.save(loc);
    }

    @Transactional
    public MechanicLocation updateSubscriptionLevel(UUID mechanicId, com.mecano.geolocation_service.entity.SubscriptionLevel level) {
        MechanicLocation loc = locationRepo.findByMechanicId(mechanicId)
                .orElseThrow(() -> new RuntimeException("Localisation introuvable"));
        loc.setSubscriptionLevel(level);
        return locationRepo.save(loc);
    }

    // ── Recherche mécaniciens proches ───────────────────────────
    public List<NearbyMechanicResponse> findNearby(NearbySearchRequest req) {
        double radius = req.getRadiusKm() != null ? req.getRadiusKm() : 10.0;
        List<MechanicLocation> locations = locationRepo.findNearbyMechanics(
                req.getLatitude(), req.getLongitude(), radius);
                
        return locations.stream()
                .map(loc -> {
                    double dist = haversine.calculateDistance(
                            req.getLatitude(), req.getLongitude(), loc.getLatitude(), loc.getLongitude());
                    return NearbyMechanicResponse.builder()
                            .mechanicId(loc.getMechanicId())
                            .latitude(loc.getLatitude())
                            .longitude(loc.getLongitude())
                            .address(loc.getAddress())
                            .city(loc.getCity())
                            .distanceKm(dist)
                            .subscriptionLevel(loc.getSubscriptionLevel())
                            .priorityScore(loc.getSubscriptionLevel().getScore())
                            .active(loc.isActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── Localisation client (automobiliste en détresse) ─────────
    @Transactional
    public ClientLocation saveClientLocation(ClientLocationRequest req) {
        ClientLocation loc = clientLocationRepo.findByRepairRequestId(req.getRepairRequestId())
                .orElse(ClientLocation.builder()
                        .clientId(req.getClientId())
                        .repairRequestId(req.getRepairRequestId())
                        .build());

        loc.setLatitude(req.getLatitude());
        loc.setLongitude(req.getLongitude());
        loc.setAddress(req.getAddress());
        loc.setActive(true);
        return clientLocationRepo.save(loc);
    }

    public ClientLocation getClientLocationByRepairRequest(UUID repairRequestId) {
        return clientLocationRepo.findByRepairRequestId(repairRequestId)
                .orElseThrow(() -> new RuntimeException(
                        "Aucune position trouvée pour la demande de panne : " + repairRequestId));
    }

    @Transactional
    public void closeClientLocation(UUID repairRequestId) {
        ClientLocation loc = getClientLocationByRepairRequest(repairRequestId);
        loc.setActive(false);
        clientLocationRepo.save(loc);
    }
}