package com.aug.flightbooking.application.event;

public record ReservationFailedEvent(Long reservationId, String reason) implements IntegrationEvent {

    @Override
    public String getEventType() {
        return "redis.reservation.failed";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
