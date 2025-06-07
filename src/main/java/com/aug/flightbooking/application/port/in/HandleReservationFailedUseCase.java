package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.event.ReservationFailedEvent;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar el evento de reserva fallida.
 */
public interface HandleReservationFailedUseCase {
    Mono<Void> handle(ReservationFailedEvent event);
}
