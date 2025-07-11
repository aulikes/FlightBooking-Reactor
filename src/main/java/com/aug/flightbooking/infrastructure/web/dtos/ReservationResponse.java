package com.aug.flightbooking.infrastructure.web.dtos;

import java.time.Instant;

public record ReservationResponse(
        Long id,
        Long flightId,
        String fullName,
        String documentId,
        String status,
        Instant createdAt
) {}

