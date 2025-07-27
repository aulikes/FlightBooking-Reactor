package com.aug.flightbooking.application.events;

public record TicketCreatedEvent(
    Long reservationId,
    String message
) implements IntegrationEvent {

    @Override
    public String getEventType() {
        return "ticket.created";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
