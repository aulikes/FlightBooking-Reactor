package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import com.aug.flightbooking.application.port.in.HandleFlightseatRejectedUseCase;
import com.aug.flightbooking.domain.model.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación que maneja eventos externos del contexto Flight
 * y actualiza el estado de las reservas según la disponibilidad.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HandleFlightseatRejectedService implements HandleFlightseatRejectedUseCase {

    private final ReservationStatusUpdater reservationStatusUpdater;
    /**
     * Maneja el evento de reserva rechazada y actualiza el estado a REJECTED.
     */
    @Override
    public Mono<Void> handle(FlightseatRejectedEvent event) {
        return reservationStatusUpdater.updateStatus(event.reservationId(), ReservationStatusAction.REJECTED);
    }
}
