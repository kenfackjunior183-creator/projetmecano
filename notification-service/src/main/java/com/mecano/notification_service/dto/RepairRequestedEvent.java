package com.mecano.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequestedEvent {
    private String automobilistEmail;
    private String automobilistFirstName;
    private String mechanicEmail;
    private String mechanicFirstName;
    private String description;
    private Double latitude;
    private Double longitude;
}
