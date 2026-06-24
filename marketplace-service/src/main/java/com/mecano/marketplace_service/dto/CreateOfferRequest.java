package com.mecano.marketplace_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOfferRequest {

    @NotBlank
    private String listingId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal offeredPrice;

    @Size(max = 500)
    private String message;
}