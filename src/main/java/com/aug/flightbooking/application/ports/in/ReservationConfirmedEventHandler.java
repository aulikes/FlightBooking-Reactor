package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.application.events.TicketCreatedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar eventos de reserva confirmada.
 */
public interface ReservationConfirmedEventHandler {
    Mono<Void> handle(TicketCreatedEvent event);
}
