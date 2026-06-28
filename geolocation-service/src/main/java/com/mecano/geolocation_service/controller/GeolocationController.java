package com.mecano.geolocation_service.controller;

import com.mecano.geolocation_service.dto.*;
import com.mecano.geolocation_service.entity.ClientLocation;
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

    // ── Localisation client (automobiliste en détresse) ────────

    /**
     * Enregistre ou met à jour la position du client pour une demande
     * de panne donnée. Appelé par l'app client au moment du signalement,
     * et peut être rappelé pour rafraîchir la position si elle change.
     */
    @PostMapping("/client-location")
    public ResponseEntity<ClientLocation> saveClientLocation(
            @Valid @RequestBody ClientLocationRequest req) {
        if ((req.getLatitude() == null || req.getLongitude() == null) && req.getAddress() != null) {
            double[] coords = nominatim.geocodeAddress(req.getAddress());
            req.setLatitude(coords[0]);
            req.setLongitude(coords[1]);
        }
        return ResponseEntity.ok(service.saveClientLocation(req));
    }

    /**
     * Récupère la position du client lié à une demande de panne précise.
     * Utilisé par le mécanicien assigné pour savoir où se rendre.
     */
    @GetMapping("/client-location/repair/{repairRequestId}")
    public ResponseEntity<ClientLocation> getClientLocation(
            @PathVariable UUID repairRequestId) {
        return ResponseEntity.ok(service.getClientLocationByRepairRequest(repairRequestId));
    }

    /**
     * Marque la position comme inactive une fois l'intervention terminée
     * (la demande de panne est close).
     */
    @PatchMapping("/client-location/repair/{repairRequestId}/close")
    public ResponseEntity<Void> closeClientLocation(
            @PathVariable UUID repairRequestId) {
        service.closeClientLocation(repairRequestId);
        return ResponseEntity.noContent().build();
    }
}