package com.mecano.subscription_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mechanic_subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MechanicSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID mechanicId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    private String stripeSessionId;
    private String stripeSubscriptionId;
    private String paymentReference;

    @PrePersist
    protected void onCreate() { startDate = LocalDateTime.now(); }
}
