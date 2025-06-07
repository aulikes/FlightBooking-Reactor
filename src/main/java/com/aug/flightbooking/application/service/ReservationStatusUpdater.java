package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.port.out.ReservationRepository;
import com.aug.flightbooking.domain.exception.ReservationChangeStatusException;
import com.aug.flightbooking.domain.model.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Componente de aplicación responsable de actualizar el estado de una reservación
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReservationStatusUpdater {

    private final ReservationRepository reservationRepository;

    /**
     * Cambia el estado de la Reserva y la actualiza en base de datos
     * @param reservationId
     * @param action
     * @return
     */
    public Mono<Void> updateStatus(Long reservationId, ReservationStatusAction action) {
        return reservationRepository.findById(reservationId)
                .flatMap(reservation -> {
                    try {
                        action.apply(reservation);
                    } catch (ReservationChangeStatusException ex) {
                        return Mono.error(ex);
                    }
                    return reservationRepository.save(reservation);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Reservation Not Found")))
                .onErrorResume(ex -> {
                    log.error(ex.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}