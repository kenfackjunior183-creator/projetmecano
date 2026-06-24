package com.mecano.geolocation_service.repository;

import com.mecano.geolocation_service.entity.MechanicLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MechanicLocationRepository extends JpaRepository<MechanicLocation, UUID> {

    Optional<MechanicLocation> findByMechanicId(UUID mechanicId);

    // Tous les mécaniciens actifs dans un rayon (formule Haversine en JPQL)
    @Query("""
        SELECT m FROM MechanicLocation m
        WHERE m.active = true
        AND (6371 * acos(
            cos(radians(:lat)) * cos(radians(m.latitude))
            * cos(radians(m.longitude) - radians(:lng))
            + sin(radians(:lat)) * sin(radians(m.latitude))
        )) <= :radiusKm
        ORDER BY m.subscriptionLevel DESC,
                 (6371 * acos(
                    cos(radians(:lat)) * cos(radians(m.latitude))
                    * cos(radians(m.longitude) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(m.latitude))
                 )) ASC
        """)
    List<MechanicLocation> findNearbyMechanics(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm);
}
