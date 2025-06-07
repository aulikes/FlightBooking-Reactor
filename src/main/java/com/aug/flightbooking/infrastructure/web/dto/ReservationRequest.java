package com.aug.flightbooking.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull Long flightId,
        @NotNull String fullName,
        @NotNull String documentId
) {}

