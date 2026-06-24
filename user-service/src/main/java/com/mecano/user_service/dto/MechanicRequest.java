package com.mecano.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class MechanicRequest {
    @NotNull private UUID authUserId;
    @NotNull private UUID userId;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    private String phone;
    private String garageName;
    private String garageAddress;
    private String specialities;

    /**
     * URL ou chemin du/des document(s) justificatif(s)
     * pour passer de USER → MÉCANICIEN.
     */
    private String justificationDocument;
}
