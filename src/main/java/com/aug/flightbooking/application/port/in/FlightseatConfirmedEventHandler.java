package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar el evento de reserva confirmada.
 */
public interface FlightseatConfirmedEventHandler {
    Mono<Void> handle(FlightseatConfirmedEvent event);
}
