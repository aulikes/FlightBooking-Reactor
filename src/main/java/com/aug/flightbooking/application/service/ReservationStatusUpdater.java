package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.exception.ReservationChangeStatusException;
import com.aug.flightbooking.domain.model.reservation.Reservation;
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
     * Busca la Resevación eb BD, Cambia el estado y la actualiza en base de datos
     */
    public Mono<Void> updateStatus(Long reservationId, ReservationStatusAction action) {
        return reservationRepository.findById(reservationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Reservation Not Found")))
                .flatMap(reservation -> updateStatus(reservation, action));
    }

    /**
     * Actualiza el estado de una reserva sin necesidad de buscarla en la base de datos.
     * Ideal para casos donde ya tienes la entidad cargada (por ejemplo, después de crearla o recibirla por evento).
     */
    public Mono<Void> updateStatus(Reservation reservation, ReservationStatusAction action) {
        return Mono.defer(() -> {
            try {
                action.apply(reservation);
                return reservationRepository.save(reservation).then();
            } catch (ReservationChangeStatusException ex) {
                return Mono.error(ex);
            }
        }).onErrorResume(ex -> {
            log.error("Error updating reservation status: {}", ex.getMessage());
            return Mono.error(ex);
        });
    }
}