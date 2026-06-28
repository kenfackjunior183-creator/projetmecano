package com.mecano.geolocation_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate; 
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;


@Entity
@Table(name = "client_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientLocation {
   
   
     @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
 
    @Column(name = "client_id", nullable = false)
    private UUID clientId;
 
    /**
     * Identifiant de la demande de panne (repair-service).
     * Permet à un mécanicien assigné de retrouver la position
     * exacte du client lié à CETTE intervention précise.
     */
    @Column(name = "repair_request_id", nullable = false, unique = true)
    private UUID repairRequestId;
 
    @Column(nullable = false)
    private Double latitude;
 
    @Column(nullable = false)
    private Double longitude;
 
    private String address;
 
    @Builder.Default
    private boolean active = true;
 
    private LocalDateTime lastUpdated;
 
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
