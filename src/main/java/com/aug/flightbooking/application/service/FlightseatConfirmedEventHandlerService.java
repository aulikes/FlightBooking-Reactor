package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.events.ReservationConfirmedEvent;
import com.aug.flightbooking.application.ports.in.FlightseatConfirmedEventHandler;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.application.ports.out.ReservationConfirmedEventPublisher;
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
public class FlightseatConfirmedEventHandlerService implements FlightseatConfirmedEventHandler {

    private final ReservationStatusUpdater reservationStatusUpdater;
    private final ReservationConfirmedEventPublisher ReservationConfirmedEventPublisher;
    private final ReservationCache reservationCache;

    /**
     * Maneja el evento de asiento confirmado y actualiza el estado de la reserva a CONFIRMED.
     */
    @Override
    public Mono<Void> handle(FlightseatConfirmedEvent event) {
        return reservationStatusUpdater.updateStatus(event.reservationId(), ReservationStatusAction.CONFIRMED)
            .onErrorResume(error -> {
                log.error("Error actualizando estado de reserva {}", event.reservationId(), error);
                return Mono.empty();
            })
            .then(
                Mono.when(
                    reservationCache.cancelTimeout(event.reservationId())
                        .onErrorResume(error -> {
                            log.error("Error cancelando timeout de reserva {}", event.reservationId(), error);
                            return Mono.empty();
                        }),
                    ReservationConfirmedEventPublisher.publish(new ReservationConfirmedEvent(event.reservationId()))
                        .onErrorResume(error -> {
                            log.error("Error publicando evento", error);
                            return Mono.empty();
                    })
                )
            );
    }
}
