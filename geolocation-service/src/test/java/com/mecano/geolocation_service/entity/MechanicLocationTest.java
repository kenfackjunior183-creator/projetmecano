package com.mecano.geolocation_service.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MechanicLocationTest {

    @Test
    void shouldBuildMechanicLocationWithDefaults() {
        UUID mechanicId = UUID.randomUUID();

        MechanicLocation loc = MechanicLocation.builder()
                .mechanicId(mechanicId)
                .latitude(4.0511)
                .longitude(9.7679)
                .build();

        assertThat(loc.getMechanicId()).isEqualTo(mechanicId);
        assertThat(loc.getLatitude()).isEqualTo(4.0511);
        assertThat(loc.getLongitude()).isEqualTo(9.7679);
        // Valeurs par défaut définies via @Builder.Default
        assertThat(loc.getInterventionRadiusKm()).isEqualTo(15.0);
        assertThat(loc.getSubscriptionLevel()).isEqualTo(SubscriptionLevel.BASIC);
        assertThat(loc.isActive()).isTrue();
    }

    @Test
    void shouldUpdateLastUpdatedOnPrePersist() {
        MechanicLocation loc = MechanicLocation.builder()
                .mechanicId(UUID.randomUUID())
                .latitude(4.05)
                .longitude(9.76)
                .build();

        assertThat(loc.getLastUpdated()).isNull();

        loc.onUpdate(); // simule le callback @PrePersist/@PreUpdate

        assertThat(loc.getLastUpdated()).isNotNull();
    }

    @Test
    void shouldAllowOverridingSubscriptionLevel() {
        MechanicLocation loc = MechanicLocation.builder()
                .mechanicId(UUID.randomUUID())
                .latitude(4.05)
                .longitude(9.76)
                .subscriptionLevel(SubscriptionLevel.GOLD)
                .build();

        assertThat(loc.getSubscriptionLevel()).isEqualTo(SubscriptionLevel.GOLD);
        assertThat(loc.getSubscriptionLevel().getScore()).isEqualTo(3);
    }
}
