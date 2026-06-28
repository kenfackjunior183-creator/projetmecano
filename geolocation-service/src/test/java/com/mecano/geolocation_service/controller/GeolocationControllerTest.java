package com.mecano.geolocation_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mecano.geolocation_service.dto.ClientLocationRequest;
import com.mecano.geolocation_service.dto.LocationRequest;
import com.mecano.geolocation_service.dto.NearbyMechanicResponse;
import com.mecano.geolocation_service.dto.NearbySearchRequest;
import com.mecano.geolocation_service.entity.ClientLocation;
import com.mecano.geolocation_service.entity.MechanicLocation;
import com.mecano.geolocation_service.entity.SubscriptionLevel;
import com.mecano.geolocation_service.security.SecurityConfig;
import com.mecano.geolocation_service.service.GeolocationService;
import com.mecano.geolocation_service.service.NominatimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste GeolocationController avec la VRAIE chaîne de sécurité
 * (SecurityConfig + JwtAuthFilter), pas une config bidon permissive.
 * Ça garantit que les règles testées sont celles réellement appliquées
 * en production : /nearby public, tout le reste authentifié.
 */
@WebMvcTest(GeolocationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class GeolocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeolocationService service;

    @MockBean
    private NominatimService nominatim;

    // ───────────── /nearby : route publique ─────────────

    @Test
    void nearby_shouldBeAccessibleWithoutAuthentication() throws Exception {
        NearbySearchRequest req = new NearbySearchRequest();
        req.setLatitude(4.0511);
        req.setLongitude(9.7679);
        req.setRadiusKm(5.0);

        when(service.findNearby(any(NearbySearchRequest.class))).thenReturn(List.of());

        mockMvc.perform(post("/api/geolocation/nearby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void nearby_shouldReturnMatchingMechanics() throws Exception {
        UUID mechanicId = UUID.randomUUID();
        NearbySearchRequest req = new NearbySearchRequest();
        req.setLatitude(4.0511);
        req.setLongitude(9.7679);

        NearbyMechanicResponse response = NearbyMechanicResponse.builder()
                .mechanicId(mechanicId)
                .latitude(4.06)
                .longitude(9.78)
                .distanceKm(1.5)
                .subscriptionLevel(SubscriptionLevel.GOLD)
                .priorityScore(3)
                .active(true)
                .build();

        when(service.findNearby(any(NearbySearchRequest.class))).thenReturn(List.of(response));

        mockMvc.perform(post("/api/geolocation/nearby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mechanicId").value(mechanicId.toString()))
                .andExpect(jsonPath("$[0].subscriptionLevel").value("GOLD"));
    }

    @Test
    void nearby_shouldReturnBadRequestWhenLatitudeMissing() throws Exception {
        String invalidJson = "{\"longitude\": 9.7679}";

        mockMvc.perform(post("/api/geolocation/nearby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ───────────── /location (mécanicien) : protégé ─────────────

    @Test
    void saveLocation_shouldBeRejectedWithoutAuthentication() throws Exception {
        LocationRequest req = new LocationRequest();
        req.setMechanicId(UUID.randomUUID());
        req.setLatitude(4.05);
        req.setLongitude(9.76);

        mockMvc.perform(post("/api/geolocation/location")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void saveLocation_shouldSucceedWhenAuthenticated() throws Exception {
        UUID mechanicId = UUID.randomUUID();
        LocationRequest req = new LocationRequest();
        req.setMechanicId(mechanicId);
        req.setLatitude(4.0511);
        req.setLongitude(9.7679);
        req.setAddress("Akwa, Douala");

        MechanicLocation saved = MechanicLocation.builder()
                .mechanicId(mechanicId)
                .latitude(4.0511)
                .longitude(9.7679)
                .address("Akwa, Douala")
                .active(true)
                .build();

        when(service.saveLocation(any(LocationRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/geolocation/location")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mechanicId").value(mechanicId.toString()));
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void saveLocation_shouldGeocodeWhenOnlyAddressProvided() throws Exception {
        UUID mechanicId = UUID.randomUUID();
        LocationRequest req = new LocationRequest();
        req.setMechanicId(mechanicId);
        req.setAddress("Akwa, Douala");
        // pas de latitude/longitude -> doit déclencher geocodeAddress()

        when(nominatim.geocodeAddress("Akwa, Douala")).thenReturn(new double[]{4.0511, 9.7679});
        when(service.saveLocation(any(LocationRequest.class))).thenAnswer(invocation -> {
            LocationRequest passed = invocation.getArgument(0);
            return MechanicLocation.builder()
                    .mechanicId(passed.getMechanicId())
                    .latitude(passed.getLatitude())
                    .longitude(passed.getLongitude())
                    .build();
        });

        mockMvc.perform(post("/api/geolocation/location")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(4.0511))
                .andExpect(jsonPath("$.longitude").value(9.7679));
    }

    // ───────────── PATCH /location/{id}/subscription : protégé ─────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSubscription_shouldSucceedWhenAuthenticated() throws Exception {
        UUID mechanicId = UUID.randomUUID();
        MechanicLocation updated = MechanicLocation.builder()
                .mechanicId(mechanicId)
                .latitude(4.05)
                .longitude(9.76)
                .subscriptionLevel(SubscriptionLevel.GOLD)
                .build();

        when(service.updateSubscriptionLevel(eq(mechanicId), eq(SubscriptionLevel.GOLD)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/geolocation/location/{mechanicId}/subscription", mechanicId)
                        .with(csrf())
                        .param("level", "GOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionLevel").value("GOLD"));
    }

    // ───────────── /client-location : protégé ─────────────

    @Test
    void clientLocation_shouldBeRejectedWithoutAuthentication() throws Exception {
        ClientLocationRequest req = new ClientLocationRequest();
        req.setClientId(UUID.randomUUID());
        req.setRepairRequestId(UUID.randomUUID());
        req.setLatitude(4.05);
        req.setLongitude(9.76);

        mockMvc.perform(post("/api/geolocation/client-location")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientLocation_shouldSaveSuccessfullyWhenAuthenticated() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID repairRequestId = UUID.randomUUID();

        ClientLocationRequest req = new ClientLocationRequest();
        req.setClientId(clientId);
        req.setRepairRequestId(repairRequestId);
        req.setLatitude(4.0511);
        req.setLongitude(9.7679);

        ClientLocation saved = ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(repairRequestId)
                .latitude(4.0511)
                .longitude(9.7679)
                .active(true)
                .build();

        when(service.saveClientLocation(any(ClientLocationRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/geolocation/client-location")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.repairRequestId").value(repairRequestId.toString()));
    }

    @Test
    void getClientLocation_shouldBeRejectedWithoutAuthentication() throws Exception {
        UUID repairRequestId = UUID.randomUUID();

        mockMvc.perform(get("/api/geolocation/client-location/repair/{id}", repairRequestId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getClientLocation_shouldReturnPositionForAssignedMechanic() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID repairRequestId = UUID.randomUUID();

        ClientLocation existing = ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(repairRequestId)
                .latitude(4.0511)
                .longitude(9.7679)
                .active(true)
                .build();

        when(service.getClientLocationByRepairRequest(repairRequestId)).thenReturn(existing);

        mockMvc.perform(get("/api/geolocation/client-location/repair/{id}", repairRequestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.repairRequestId").value(repairRequestId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void closeClientLocation_shouldReturnNoContent() throws Exception {
        UUID repairRequestId = UUID.randomUUID();

        mockMvc.perform(patch("/api/geolocation/client-location/repair/{id}/close", repairRequestId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
