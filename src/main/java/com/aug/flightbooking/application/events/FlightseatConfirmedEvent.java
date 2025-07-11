package com.aug.flightbooking.application.events;

public record FlightseatConfirmedEvent(Long reservationId) implements IntegrationEvent
{
    @Override
    public String getEventType() {
        return "flightseat.confirmed";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
