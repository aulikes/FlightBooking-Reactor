package com.aug.flightbooking.infrastructure.web.dtos;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull Long flightId,
        @NotNull String fullName,
        @NotNull String documentId
) {}

