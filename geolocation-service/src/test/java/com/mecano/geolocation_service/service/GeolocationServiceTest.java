package com.mecano.geolocation_service.service;

import com.mecano.geolocation_service.dto.ClientLocationRequest;
import com.mecano.geolocation_service.dto.LocationRequest;
import com.mecano.geolocation_service.dto.NearbyMechanicResponse;
import com.mecano.geolocation_service.dto.NearbySearchRequest;
import com.mecano.geolocation_service.entity.ClientLocation;
import com.mecano.geolocation_service.entity.MechanicLocation;
import com.mecano.geolocation_service.entity.SubscriptionLevel;
import com.mecano.geolocation_service.repository.ClientLocationRepository;
import com.mecano.geolocation_service.repository.MechanicLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeolocationServiceTest {

    @Mock
    private MechanicLocationRepository mechanicRepo;

    @Mock
    private ClientLocationRepository clientLocationRepo;

    // HaversineService est pur (aucune dépendance externe) : on utilise
    // une vraie instance plutôt qu'un mock pour des assertions fiables.
    private final HaversineService haversine = new HaversineService();

    // Construit manuellement (pas via @InjectMocks) car HaversineService
    // n'est pas un mock — on veut le vrai calcul de distance dans ces tests.
    private GeolocationService service;

    private UUID mechanicId;
    private UUID clientId;
    private UUID repairRequestId;

    @BeforeEach
    void setUp() {
        mechanicId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        repairRequestId = UUID.randomUUID();

        // @InjectMocks ne peut pas injecter un objet "réel" automatiquement,
        // donc on le remplace manuellement après construction.
        service = new GeolocationService(mechanicRepo, clientLocationRepo, haversine);
    }

    // ───────────── saveLocation (mécanicien) ─────────────

    @Test
    void shouldCreateNewMechanicLocation() {
        LocationRequest req = new LocationRequest();
        req.setMechanicId(mechanicId);
        req.setLatitude(4.0511);
        req.setLongitude(9.7679);
        req.setAddress("Akwa, Douala");
        req.setCity("Douala");

        when(mechanicRepo.findByMechanicId(mechanicId)).thenReturn(Optional.empty());
        when(mechanicRepo.save(any(MechanicLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MechanicLocation result = service.saveLocation(req);

        assertThat(result.getMechanicId()).isEqualTo(mechanicId);
        assertThat(result.getLatitude()).isEqualTo(4.0511);
        assertThat(result.isActive()).isTrue();
        verify(mechanicRepo).save(any(MechanicLocation.class));
    }

    @Test
    void shouldUpdateExistingMechanicLocation() {
        MechanicLocation existing = MechanicLocation.builder()
                .mechanicId(mechanicId)
                .latitude(4.0)
                .longitude(9.7)
                .build();

        LocationRequest req = new LocationRequest();
        req.setMechanicId(mechanicId);
        req.setLatitude(4.0600);
        req.setLongitude(9.7800);

        when(mechanicRepo.findByMechanicId(mechanicId)).thenReturn(Optional.of(existing));
        when(mechanicRepo.save(any(MechanicLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MechanicLocation result = service.saveLocation(req);

        assertThat(result.getLatitude()).isEqualTo(4.0600);
        assertThat(result.getLongitude()).isEqualTo(9.7800);
        assertThat(result.getMechanicId()).isEqualTo(mechanicId);
        verify(mechanicRepo, times(1)).save(any(MechanicLocation.class));
    }

    // ───────────── updateSubscriptionLevel ─────────────

    @Test
    void shouldUpdateSubscriptionLevel() {
        MechanicLocation existing = MechanicLocation.builder()
                .mechanicId(mechanicId)
                .latitude(4.0)
                .longitude(9.7)
                .subscriptionLevel(SubscriptionLevel.BASIC)
                .build();

        when(mechanicRepo.findByMechanicId(mechanicId)).thenReturn(Optional.of(existing));
        when(mechanicRepo.save(any(MechanicLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MechanicLocation result = service.updateSubscriptionLevel(mechanicId, SubscriptionLevel.GOLD);

        assertThat(result.getSubscriptionLevel()).isEqualTo(SubscriptionLevel.GOLD);
    }

    @Test
    void shouldThrowWhenUpdatingSubscriptionOfUnknownMechanic() {
        when(mechanicRepo.findByMechanicId(mechanicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSubscriptionLevel(mechanicId, SubscriptionLevel.GOLD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Localisation introuvable");
    }

    // ───────────── findNearby ─────────────

    @Test
    void shouldFindNearbyMechanicsSortedAndMapped() {
        MechanicLocation m1 = MechanicLocation.builder()
                .mechanicId(mechanicId)
                .latitude(4.0600)
                .longitude(9.7800)
                .address("Akwa")
                .city("Douala")
                .subscriptionLevel(SubscriptionLevel.GOLD)
                .active(true)
                .build();

        NearbySearchRequest req = new NearbySearchRequest();
        req.setLatitude(4.0511);
        req.setLongitude(9.7679);
        req.setRadiusKm(5.0);

        when(mechanicRepo.findNearbyMechanics(4.0511, 9.7679, 5.0))
                .thenReturn(List.of(m1));

        List<NearbyMechanicResponse> result = service.findNearby(req);

        assertThat(result).hasSize(1);
        NearbyMechanicResponse response = result.get(0);
        assertThat(response.getMechanicId()).isEqualTo(mechanicId);
        assertThat(response.getSubscriptionLevel()).isEqualTo(SubscriptionLevel.GOLD);
        assertThat(response.getPriorityScore()).isEqualTo(3);
        assertThat(response.getDistanceKm()).isGreaterThan(0);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void shouldUseDefaultRadiusOf10KmWhenNotProvided() {
        NearbySearchRequest req = new NearbySearchRequest();
        req.setLatitude(4.05);
        req.setLongitude(9.76);
        req.setRadiusKm(null);

        when(mechanicRepo.findNearbyMechanics(4.05, 9.76, 10.0)).thenReturn(List.of());

        service.findNearby(req);

        verify(mechanicRepo).findNearbyMechanics(4.05, 9.76, 10.0);
    }

    // ───────────── saveClientLocation ─────────────

    @Test
    void shouldCreateNewClientLocationForNewRepairRequest() {
        ClientLocationRequest req = new ClientLocationRequest();
        req.setClientId(clientId);
        req.setRepairRequestId(repairRequestId);
        req.setLatitude(4.05);
        req.setLongitude(9.76);

        when(clientLocationRepo.findByRepairRequestId(repairRequestId))
                .thenReturn(Optional.empty());
        when(clientLocationRepo.save(any(ClientLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClientLocation result = service.saveClientLocation(req);

        assertThat(result.getClientId()).isEqualTo(clientId);
        assertThat(result.getRepairRequestId()).isEqualTo(repairRequestId);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void shouldUpdateClientLocationWhenRepairRequestAlreadyHasOne() {
        ClientLocation existing = ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(repairRequestId)
                .latitude(4.0)
                .longitude(9.7)
                .build();

        ClientLocationRequest req = new ClientLocationRequest();
        req.setClientId(clientId);
        req.setRepairRequestId(repairRequestId);
        req.setLatitude(4.09);
        req.setLongitude(9.79);

        when(clientLocationRepo.findByRepairRequestId(repairRequestId))
                .thenReturn(Optional.of(existing));
        when(clientLocationRepo.save(any(ClientLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClientLocation result = service.saveClientLocation(req);

        assertThat(result.getLatitude()).isEqualTo(4.09);
        assertThat(result.getLongitude()).isEqualTo(9.79);
    }

    // ───────────── getClientLocationByRepairRequest ─────────────

    @Test
    void shouldRetrieveClientLocationByRepairRequest() {
        ClientLocation existing = ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(repairRequestId)
                .latitude(4.05)
                .longitude(9.76)
                .build();

        when(clientLocationRepo.findByRepairRequestId(repairRequestId))
                .thenReturn(Optional.of(existing));

        ClientLocation result = service.getClientLocationByRepairRequest(repairRequestId);

        assertThat(result.getClientId()).isEqualTo(clientId);
    }

    @Test
    void shouldThrowWhenClientLocationNotFoundForRepairRequest() {
        when(clientLocationRepo.findByRepairRequestId(repairRequestId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getClientLocationByRepairRequest(repairRequestId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(repairRequestId.toString());
    }

    // ───────────── closeClientLocation ─────────────

    @Test
    void shouldDeactivateClientLocationOnClose() {
        ClientLocation existing = ClientLocation.builder()
                .clientId(clientId)
                .repairRequestId(repairRequestId)
                .latitude(4.05)
                .longitude(9.76)
                .build();
        existing.setActive(true);

        when(clientLocationRepo.findByRepairRequestId(repairRequestId))
                .thenReturn(Optional.of(existing));
        when(clientLocationRepo.save(any(ClientLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.closeClientLocation(repairRequestId);

        verify(clientLocationRepo).save(argThat(loc -> !loc.isActive()));
    }
}
