package com.aug.flightbooking.application.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para registrar reservas creadas en Redis y así monitorear expiraciones.
 */
public interface ReservationCache {

    /**
     * Registra una reserva para que sea monitoreada por timeout.
     */
    Mono<Void> registerTimeout(Long reservationId);

    /**
     * Cancela el timeout de la reserva cuando ya tuvo respuesta
     */
    Mono<Void> cancelTimeout(Long reservationId);
}
