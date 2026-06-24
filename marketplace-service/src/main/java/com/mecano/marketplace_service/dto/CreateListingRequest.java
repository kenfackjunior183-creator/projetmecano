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
public class CreateListingRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @NotBlank
    private String category;

    @NotBlank
    private String brand;

    private String model;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @Min(1)
    private Integer quantity;

    private String condition;

    private Boolean negotiable;

    private String imageUrl;

    @NotBlank
    private String location;
}