package com.mecano.geolocation_service.controller;

import com.mecano.geolocation_service.dto.*;
import com.mecano.geolocation_service.entity.MechanicLocation;
import com.mecano.geolocation_service.entity.SubscriptionLevel;
import com.mecano.geolocation_service.service.GeolocationService;
import com.mecano.geolocation_service.service.NominatimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/geolocation")
@RequiredArgsConstructor
public class GeolocationController {

    private final GeolocationService service;
    private final NominatimService nominatim;

    // ── Localisations mécaniciens ──────────────────────────────
    @PostMapping("/location")
    public ResponseEntity<MechanicLocation> saveLocation(@Valid @RequestBody LocationRequest req) {
        // Si l'adresse est fournie mais pas les coordonnées, on géocode via Nominatim
        if ((req.getLatitude() == null || req.getLongitude() == null) && req.getAddress() != null) {
            double[] coords = nominatim.geocodeAddress(req.getAddress());
            req.setLatitude(coords[0]);
            req.setLongitude(coords[1]);
        }
        // Si les coordonnées sont fournies mais pas l'adresse, géocodage inverse
        else if (req.getLatitude() != null && req.getLongitude() != null && req.getAddress() == null) {
            String addr = nominatim.reverseGeocode(req.getLatitude(), req.getLongitude());
            req.setAddress(addr);
        }
        return ResponseEntity.ok(service.saveLocation(req));
    }

    @PatchMapping("/location/{mechanicId}/subscription")
    public ResponseEntity<MechanicLocation> updateSubscription(
            @PathVariable UUID mechanicId,
            @RequestParam SubscriptionLevel level) {
        return ResponseEntity.ok(service.updateSubscriptionLevel(mechanicId, level));
    }

    @PostMapping("/nearby")
    public ResponseEntity<List<NearbyMechanicResponse>> getNearbyMechanics(
            @Valid @RequestBody NearbySearchRequest req) {
        return ResponseEntity.ok(service.findNearby(req));
    }
}
