package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar el evento de reserva rechazada.
 */
public interface HandleFlightseatRejectedUseCase {
    Mono<Void> handle(FlightseatRejectedEvent event);
}
