package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar eventos de reserva creada.
 */
public interface ReservationCreatedEventHandler {
    Mono<Void> handle(ReservationCreatedEvent event);
}
