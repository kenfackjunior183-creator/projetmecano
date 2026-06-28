package com.mecano.geolocation_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class ClientLocationRequest {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private UUID clientId;

    @NotNull(message = "L'identifiant de la demande de panne est obligatoire")
    private UUID repairRequestId;

    private Double latitude;
    private Double longitude;

    // Optionnel : si fournie sans coordonnées, géocodée via Nominatim
    private String address;
}
