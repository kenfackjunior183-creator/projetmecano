package com.mecano.geolocation_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NearbySearchRequest {
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    private Double radiusKm = 10.0;
}
