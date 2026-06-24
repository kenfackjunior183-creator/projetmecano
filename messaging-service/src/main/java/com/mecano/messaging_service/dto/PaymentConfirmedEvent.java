package com.mecano.messaging_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {
    private String email;
    private String firstName;
    private String planLevel;
    private String amount;
    private String currency;
}