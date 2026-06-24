package com.mecano.user_service.dto;

import com.mecano.user_service.entity.SubscriptionLevel;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MechanicResponse {
    private UUID id;
    private UUID authUserId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String garageName;
    private String garageAddress;
    private String specialities;
    private SubscriptionLevel subscriptionLevel;
    private int priorityScore;
    private boolean available;
}
