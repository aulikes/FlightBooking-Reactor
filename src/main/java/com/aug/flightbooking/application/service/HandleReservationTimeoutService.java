package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.event.ReservationFailedEvent;
import com.aug.flightbooking.application.port.in.HandleReservationFailedUseCase;
import com.aug.flightbooking.application.port.out.ReservationRepository;
import com.aug.flightbooking.domain.model.reservation.Reservation;
import com.aug.flightbooking.domain.model.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandleReservationTimeoutService implements HandleReservationFailedUseCase {

    private final ReservationStatusUpdater reservationStatusUpdater;

    private final ReservationRepository reservationRepository;

    public Mono<Void> markExpiredReservations() {
        Instant expirationThreshold = Instant.now().minusSeconds(5 * 60);
        return reservationRepository.findReservationsCreatedBefore(expirationThreshold)
            .flatMap(this::failReservation)
            .then();
    }

    private Mono<Void> failReservation(Reservation reservation) {
        log.error("Fail Reservation: {}", reservation);
        ReservationStatusAction.FAILED.apply(reservation);
        return reservationRepository.save(reservation).then();
    }

    /**
     * Maneja el evento de reserva fallida y actualiza el estado a FAILED.
     */
    @Override
    public Mono<Void> handle(ReservationFailedEvent event) {
        return reservationStatusUpdater.updateStatus(event.reservationId(), ReservationStatusAction.FAILED);
    }
}
