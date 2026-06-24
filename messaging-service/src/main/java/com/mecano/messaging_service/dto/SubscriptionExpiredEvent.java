package com.mecano.messaging_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionExpiredEvent {
    private String email;
    private String firstName;
    private String planLevel;
}