package com.aug.flightbooking.application.event;

public record FlightseatRejectedEvent(Long reservationId, String reason) implements IntegrationEvent {

    @Override
    public String getEventType() {
        return "FlightseatRejectedEvent";
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
