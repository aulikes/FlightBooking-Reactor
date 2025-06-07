package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.port.in.HandleFlightseatConfirmedUseCase;
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
public class HandleFlightseatConfirmedService implements HandleFlightseatConfirmedUseCase {

    private final ReservationStatusUpdater reservationStatusUpdater;

    /**
     * Maneja el evento de reserva confirmada y actualiza el estado a CONFIRMED.
     */
    @Override
    public Mono<Void> handle(FlightseatConfirmedEvent event) {
        return reservationStatusUpdater.updateStatus(event.reservationId(), ReservationStatusAction.CONFIRMED);
    }
}
