package com.aug.flightbooking.infrastructure.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull Long flightId,
        @NotBlank String fullName,
        @NotBlank String documentId
) {}

