package com.aug.flightbooking.application.event;

public record FlightseatConfirmedEvent(Long reservationId) implements IntegrationEvent
{
    @Override
    public String getEventType() {
        return "flight.reservation.confirmed";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
