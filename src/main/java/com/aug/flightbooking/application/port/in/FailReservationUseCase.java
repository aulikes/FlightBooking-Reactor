package com.aug.flightbooking.application.port.in;

import reactor.core.publisher.Mono;

/**
 * Caso de uso para manejar el evento de reserva fallida.
 */
public interface FailReservationUseCase {
    Mono<Void> failReservations(long timeSeconds);
}