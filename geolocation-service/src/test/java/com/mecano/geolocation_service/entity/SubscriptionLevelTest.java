package com.mecano.geolocation_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionLevelTest {

    @Test
    void shouldHaveCorrectScoreOrdering() {
        assertThat(SubscriptionLevel.BASIC.getScore()).isEqualTo(1);
        assertThat(SubscriptionLevel.SILVER.getScore()).isEqualTo(2);
        assertThat(SubscriptionLevel.GOLD.getScore()).isEqualTo(3);
    }

    @Test
    void shouldHaveExactlyThreeLevels() {
        assertThat(SubscriptionLevel.values()).hasSize(3);
    }
}
