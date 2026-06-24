package com.mecano.geolocation_service.dto;

import com.mecano.geolocation_service.entity.SubscriptionLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class LocationRequest {
    @NotNull private UUID mechanicId;
     private Double latitude;
     private Double longitude;
    private String address;
    private String city;
    private Double interventionRadiusKm = 15.0;
    private SubscriptionLevel subscriptionLevel = SubscriptionLevel.BASIC;
}
