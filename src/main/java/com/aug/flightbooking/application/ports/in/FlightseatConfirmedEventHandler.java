package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar el evento de reserva confirmada.
 */
public interface FlightseatConfirmedEventHandler {

  Mono<Void> handle(FlightseatConfirmedEvent event);
}
