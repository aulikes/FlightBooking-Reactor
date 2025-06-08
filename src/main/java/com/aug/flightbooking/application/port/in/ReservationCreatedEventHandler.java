package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar eventos de reserva creada.
 */
public interface ReservationCreatedEventHandler {
    Mono<Void> handle(ReservationCreatedEvent event);
}
