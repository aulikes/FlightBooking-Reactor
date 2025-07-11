package com.aug.flightbooking.application.events;

public record ReservationCreatedEvent(
    Long reservationId,
    Long flightId,
    String fullName,
    String documentId
) implements IntegrationEvent {

    @Override
    public String getEventType() {
        return "reservation.created";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
