package com.mecano.geolocation_service.repository;

import com.mecano.geolocation_service.entity.ClientLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClientLocationRepositoryTest {

    @Autowired
    private ClientLocationRepository repository;

    private UUID clientId;
    private UUID repairRequestId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        clientId = UUID.randomUUID();
        repairRequestId = UUID.randomUUID();

        repository.save(ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(repairRequestId)
                .latitude(4.0511)
                .longitude(9.7679)
                .address("Akwa, Douala")
                .active(true)
                .build());
    }

    @Test
    void shouldFindByRepairRequestId() {
        Optional<ClientLocation> found = repository.findByRepairRequestId(repairRequestId);

        assertThat(found).isPresent();
        assertThat(found.get().getClientId()).isEqualTo(clientId);
        assertThat(found.get().getAddress()).isEqualTo("Akwa, Douala");
    }

    @Test
    void shouldReturnEmptyForUnknownRepairRequestId() {
        Optional<ClientLocation> found = repository.findByRepairRequestId(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindMostRecentActiveLocationForClient() {
        // Une deuxième demande de panne, plus récente, pour le même client
        UUID secondRepairRequest = UUID.randomUUID();
        repository.save(ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(secondRepairRequest)
                .latitude(4.0700)
                .longitude(9.7900)
                .active(true)
                .build());

        Optional<ClientLocation> mostRecent =
                repository.findFirstByClientIdAndActiveTrueOrderByLastUpdatedDesc(clientId);

        assertThat(mostRecent).isPresent();
        assertThat(mostRecent.get().getRepairRequestId()).isEqualTo(secondRepairRequest);
    }

    @Test
    void shouldIgnoreInactiveLocationsWhenSearchingByClientId() {
        ClientLocation existing = repository.findByRepairRequestId(repairRequestId).orElseThrow();
        existing.setActive(false);
        repository.save(existing);

        Optional<ClientLocation> active =
                repository.findFirstByClientIdAndActiveTrueOrderByLastUpdatedDesc(clientId);

        assertThat(active).isEmpty();
    }

    @Test
    void shouldEnforceUniqueRepairRequestId() {
        // repair_request_id est marqué unique=true dans l'entité :
        // une deuxième position pour la MÊME demande de panne doit
        // mettre à jour la ligne existante, pas en créer une nouvelle
        // (cette règle est appliquée au niveau service, pas du repository,
        // mais on vérifie ici que la contrainte unique est bien respectée
        // par une simple recherche après écrasement).
        ClientLocation existing = repository.findByRepairRequestId(repairRequestId).orElseThrow();
        existing.setLatitude(5.0);
        repository.save(existing);

        Optional<ClientLocation> updated = repository.findByRepairRequestId(repairRequestId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getLatitude()).isEqualTo(5.0);
        assertThat(repository.count()).isEqualTo(1);
    }
}
