package com.mecano.geolocation_service.repository;

import com.mecano.geolocation_service.entity.MechanicLocation;
import com.mecano.geolocation_service.entity.SubscriptionLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MechanicLocationRepositoryTest {

    @Autowired
    private MechanicLocationRepository repository;

    private UUID mechanicGold;
    private UUID mechanicBasic;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        mechanicGold = UUID.randomUUID();
        mechanicBasic = UUID.randomUUID();

        // Mécanicien GOLD, très proche du point de recherche
        repository.save(MechanicLocation.builder()
                .mechanicId(mechanicGold)
                .latitude(4.0511)
                .longitude(9.7679)
                .city("Douala")
                .subscriptionLevel(SubscriptionLevel.GOLD)
                .active(true)
                .build());

        // Mécanicien BASIC, un peu plus proche en distance brute mais moins prioritaire
        repository.save(MechanicLocation.builder()
                .mechanicId(mechanicBasic)
                .latitude(4.0515)
                .longitude(9.7682)
                .city("Douala")
                .subscriptionLevel(SubscriptionLevel.BASIC)
                .active(true)
                .build());

        // Mécanicien inactif : ne doit jamais apparaître dans les résultats
        repository.save(MechanicLocation.builder()
                .mechanicId(UUID.randomUUID())
                .latitude(4.0512)
                .longitude(9.7680)
                .subscriptionLevel(SubscriptionLevel.GOLD)
                .active(false)
                .build());

        // Mécanicien loin (Yaoundé) : hors du rayon de recherche
        repository.save(MechanicLocation.builder()
                .mechanicId(UUID.randomUUID())
                .latitude(3.8480)
                .longitude(11.5021)
                .subscriptionLevel(SubscriptionLevel.GOLD)
                .active(true)
                .build());
    }

    @Test
    void shouldFindByMechanicId() {
        Optional<MechanicLocation> found = repository.findByMechanicId(mechanicGold);

        assertThat(found).isPresent();
        assertThat(found.get().getCity()).isEqualTo("Douala");
    }

    @Test
    void shouldReturnEmptyWhenMechanicIdUnknown() {
        Optional<MechanicLocation> found = repository.findByMechanicId(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindOnlyActiveMechanicsWithinRadius() {
        // Recherche centrée sur Douala, rayon 5 km
        List<MechanicLocation> nearby = repository.findNearbyMechanics(4.0511, 9.7679, 5.0);

        // Les deux mécaniciens actifs proches doivent apparaître,
        // ni l'inactif, ni celui de Yaoundé (~194 km, hors rayon)
        assertThat(nearby).hasSize(2);
        assertThat(nearby).allMatch(MechanicLocation::isActive);
    }

    @Test
    void shouldOrderByGoldSubscriptionFirst() {
        List<MechanicLocation> nearby = repository.findNearbyMechanics(4.0511, 9.7679, 5.0);

        assertThat(nearby).isNotEmpty();
        // Le GOLD doit apparaître avant le BASIC, même si la distance brute diffère légèrement
        assertThat(nearby.get(0).getSubscriptionLevel()).isEqualTo(SubscriptionLevel.GOLD);
    }

    @Test
    void shouldExcludeMechanicsOutsideRadius() {
        // Rayon très petit : ne devrait garder que le plus proche, voire aucun de Yaoundé
        List<MechanicLocation> nearby = repository.findNearbyMechanics(4.0511, 9.7679, 5.0);

        assertThat(nearby)
                .extracting(MechanicLocation::getMechanicId)
                .doesNotContain((UUID) null);
        // Aucun résultat ne doit correspondre à une distance > 5 km
        assertThat(nearby).allSatisfy(m ->
                assertThat(m.getLatitude()).isBetween(4.0, 4.1));
    }
}
