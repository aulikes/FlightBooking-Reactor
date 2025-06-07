package com.aug.flightbooking.application.event;

import java.time.Instant;

public record ReservationFailedEvent(
    Long reservationId,
    String reason
) implements IntegrationEvent {

    @Override
    public String getEventType() {
        return "ReservationFailedEvent";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
