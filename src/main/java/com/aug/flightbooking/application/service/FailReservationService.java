package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.port.in.FailReservationUseCase;
import com.aug.flightbooking.application.port.out.ReservationCache;
import com.aug.flightbooking.application.port.out.ReservationRepository;
import com.aug.flightbooking.domain.model.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Caso de uso reactivo para fallar las reservas que no han sido contestadas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FailReservationService implements FailReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationCache reservationCache; // puerto de salida (redis en infra)
    private final ReservationStatusUpdater reservationStatusUpdater;

    @Override
    public Mono<Void> failReservations(long timeSeconds) {
        Instant expirationThreshold = Instant.now().minusSeconds(timeSeconds);
        return reservationRepository.findReservationsCreatedBefore(expirationThreshold)
            .flatMap(reservation -> {
                Long reservationId = reservation.getId();
                // Buscar en cache
                return reservationCache.get(reservationId)
                    .flatMap(cachedValue -> {
                        if (cachedValue != null) { // Si existe en caché, cambia el estado
                            return reservationStatusUpdater.updateStatus(
                                    reservation, ReservationStatusAction.FAILED);
                        } else {
                            return Mono.empty(); // No hacer nada si no está en cache
                        }
                    });
            })
            .then();
    }
}
