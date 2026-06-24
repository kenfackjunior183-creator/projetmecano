package com.mecano.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AutomobilistRequest {
    @NotNull private UUID authUserId;
    @NotNull private UUID userId;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    private String phone;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehiclePlate;

    /**
     * URL ou chemin du permis de conduire.
     * C'est la fourniture de ce document qui déclenche le passage
     * du rôle USER → AUTOMOBILIST.
     */
    private String drivingLicenseDocument;
}
