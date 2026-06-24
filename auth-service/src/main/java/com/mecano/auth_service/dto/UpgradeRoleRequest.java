package com.mecano.auth_service.dto;

import com.mecano.auth_service.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpgradeRoleRequest {

    @NotNull(message = "ID utilisateur obligatoire")
    private String userId;

    @NotNull(message = "Nouveau rôle obligatoire")
    private Role newRole;
}