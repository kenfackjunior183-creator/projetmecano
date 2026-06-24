package com.mecano.geolocation_service.dto;

import com.mecano.geolocation_service.entity.SubscriptionLevel;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NearbyMechanicResponse {
    private UUID mechanicId;
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private Double distanceKm;
    private SubscriptionLevel subscriptionLevel;
    private int priorityScore;
    private boolean active;
}
