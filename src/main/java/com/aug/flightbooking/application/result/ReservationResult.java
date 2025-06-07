package com.aug.flightbooking.application.result;

import java.time.Instant;

public record ReservationResult(
        Long id,
        Long flightId,
        String fullName,
        String documentId,
        String status,
        Instant createdAt
) {
}


