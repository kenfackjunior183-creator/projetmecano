package com.mecano.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mechanics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mechanic {

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
    private String garageName;
    private String garageAddress;
    private String specialities;

    /**
     * URL ou chemin du/des document(s) justificatif(s) pour passer au rôle MÉCANICIEN
     * (ex: diplôme, certification, attestation professionnelle, etc.).
     */
    private String justificationDocument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionLevel subscriptionLevel = SubscriptionLevel.BASIC;

    @Builder.Default
    private boolean available = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}