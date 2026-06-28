package com.mecano.geolocation_service.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientLocationTest {

    @Test
    void shouldBuildClientLocationWithDefaults() {
        UUID clientId = UUID.randomUUID();
        UUID repairRequestId = UUID.randomUUID();

        ClientLocation loc = ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(repairRequestId)
                .latitude(4.0511)
                .longitude(9.7679)
                .build();

        assertThat(loc.getClientId()).isEqualTo(clientId);
        assertThat(loc.getRepairRequestId()).isEqualTo(repairRequestId);
        assertThat(loc.getLatitude()).isEqualTo(4.0511);
        assertThat(loc.getLongitude()).isEqualTo(9.7679);
        // active = true par défaut via @Builder.Default
        assertThat(loc.isActive()).isTrue();
    }

    @Test
    void shouldSetLastUpdatedOnUpdateCallback() {
        ClientLocation loc = ClientLocation.builder()
                .clientId(UUID.randomUUID())
                .repairRequestId(UUID.randomUUID())
                .latitude(4.05)
                .longitude(9.76)
                .build();

        assertThat(loc.getLastUpdated()).isNull();

        loc.onUpdate();

        assertThat(loc.getLastUpdated()).isNotNull();
    }

    @Test
    void shouldAllowDeactivation() {
        ClientLocation loc = ClientLocation.builder()
                .clientId(UUID.randomUUID())
                .repairRequestId(UUID.randomUUID())
                .latitude(4.05)
                .longitude(9.76)
                .build();

        loc.setActive(false);

        assertThat(loc.isActive()).isFalse();
    }
}
