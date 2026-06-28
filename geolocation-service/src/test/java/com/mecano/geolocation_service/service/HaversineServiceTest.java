package com.mecano.geolocation_service.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HaversineServiceTest {

    private final HaversineService haversine = new HaversineService();

    @Test
    void shouldReturnZeroForIdenticalPoints() {
        double distance = haversine.calculateDistance(4.05, 9.76, 4.05, 9.76);
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void shouldCalculateShortDistanceWithinDouala() {
        // Deux points proches à Douala
        double distance = haversine.calculateDistance(4.0511, 9.7679, 4.0600, 9.7800);
        // ~1.67 km, on tolère une petite marge
        assertThat(distance).isCloseTo(1.67, within(0.05));
    }

    @Test
    void shouldCalculateLongDistanceDoualaToYaounde() {
        double distance = haversine.calculateDistance(4.0511, 9.7679, 3.8480, 11.5021);
        // ~193-194 km à vol d'oiseau
        assertThat(distance).isCloseTo(193.7, within(1.0));
    }

    @Test
    void shouldRoundResultToTwoDecimals() {
        double distance = haversine.calculateDistance(4.0511, 9.7679, 4.0600, 9.7800);
        double rounded = Math.round(distance * 100.0) / 100.0;
        assertThat(distance).isEqualTo(rounded);
    }
}
