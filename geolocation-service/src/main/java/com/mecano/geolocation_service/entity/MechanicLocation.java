package com.mecano.geolocation_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mechanic_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MechanicLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID mechanicId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String address;
    private String city;

    // Rayon d'intervention en km
    @Builder.Default
    private Double interventionRadiusKm = 15.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionLevel subscriptionLevel = SubscriptionLevel.BASIC;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { lastUpdated = LocalDateTime.now(); }
}
