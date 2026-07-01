package com.mecano.notification_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequest {
    @NotNull
    private UUID repairRequestId;

    @NotNull
    private UUID automobilistId;

    @NotNull
    private UUID mechanicId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}