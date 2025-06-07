package com.aug.flightbooking.application.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para registrar reservas creadas en Redis y así monitorear expiraciones.
 */
public interface ReservationCache {

    /**
     * Registra una reserva para que sea monitoreada por timeout.
     * @param reservationId ID de la reserva
     */
    Mono<Void> registerTimeout(Long reservationId);
}
