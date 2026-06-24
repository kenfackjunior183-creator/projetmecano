package com.mecano.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "automobilists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Automobilist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID authUserId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String vehicleBrand;
    private String vehicleModel;
    private String vehiclePlate;

    /**
     * URL ou chemin du permis de conduire fourni par l'utilisateur.
     * Une fois renseigné, le rôle passe de USER à AUTOMOBILIST.
     */
    private String drivingLicenseDocument;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
