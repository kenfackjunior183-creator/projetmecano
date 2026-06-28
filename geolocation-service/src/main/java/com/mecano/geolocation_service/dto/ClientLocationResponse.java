package com.mecano.geolocation_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientLocationResponse {

    private UUID clientId;
    private UUID repairRequestId;
    private Double latitude;
    private Double longitude;
    private String address;
    private boolean active;
    private LocalDateTime lastUpdated;

}
