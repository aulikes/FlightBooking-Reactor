package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.domain.models.ticket.Ticket;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar eventos de reserva creada.
 */
public interface ReservationEmittedEventHandler {
    Mono<Ticket> handle(ReservationEmittedEvent event);
}
