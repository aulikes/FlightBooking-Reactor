package com.aug.flightbooking.application.events;

public record ReservationFailedEvent(Long reservationId, String reason) implements IntegrationEvent {

    @Override
    public String getEventType() {
        return "reservation.failed";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
