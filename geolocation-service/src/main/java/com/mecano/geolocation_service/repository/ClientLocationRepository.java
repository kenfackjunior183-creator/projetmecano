package com.mecano.geolocation_service.repository;

import com.mecano.geolocation_service.entity.ClientLocation;
import com.mecano.geolocation_service.entity.ClientLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientLocationRepository extends JpaRepository<ClientLocation, UUID> {

    Optional<ClientLocation> findByRepairRequestId(UUID repairRequestId);

    Optional<ClientLocation> findFirstByClientIdAndActiveTrueOrderByLastUpdatedDesc(UUID clientId);
}
