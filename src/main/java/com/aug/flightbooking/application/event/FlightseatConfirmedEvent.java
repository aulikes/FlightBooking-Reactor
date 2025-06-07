package com.aug.flightbooking.application.event;

public record FlightseatConfirmedEvent(Long reservationId) implements IntegrationEvent
{
    @Override
    public String getEventType() {
        return "FlightseatConfirmedEvent";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
