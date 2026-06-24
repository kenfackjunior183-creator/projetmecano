package com.mecano.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRequest {
    @NotNull(message = "authUserId obligatoire")
    private UUID authUserId;

    @NotBlank(message = "Nom obligatoire")
    private String name;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email obligatoire")
    private String email;
}