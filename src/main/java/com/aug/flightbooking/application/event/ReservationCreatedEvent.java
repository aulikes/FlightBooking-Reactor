package com.aug.flightbooking.application.event;

import java.time.Instant;

public record ReservationCreatedEvent(
    Long reservationId,
    Long flightId,
    String fullName,
    String documentId,
    Instant createdAt
) {}
