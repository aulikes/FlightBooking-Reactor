package com.aug.flightbooking.infrastructure.web.dtos;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull Long ticketId,
        @NotNull Long millisecondInstant
) {}

