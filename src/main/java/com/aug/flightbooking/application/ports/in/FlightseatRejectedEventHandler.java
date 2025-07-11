package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar el evento de reserva rechazada.
 */
public interface FlightseatRejectedEventHandler {
    Mono<Void> handle(FlightseatRejectedEvent event);
}
