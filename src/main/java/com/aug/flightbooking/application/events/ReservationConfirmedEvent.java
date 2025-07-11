package com.aug.flightbooking.application.events;

public record ReservationConfirmedEvent(
    Long reservationId
) implements IntegrationEvent {

    @Override
    public String getEventType() {
        return "reservation.confirmated";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
