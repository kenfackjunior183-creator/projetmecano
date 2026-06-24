package com.mecano.admin_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String metricName;

    @Column(nullable = false)
    private Long metricValue;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @PrePersist
    protected void onCreate() {
        if (collectedAt == null) {
            collectedAt = LocalDateTime.now();
        }
    }
}