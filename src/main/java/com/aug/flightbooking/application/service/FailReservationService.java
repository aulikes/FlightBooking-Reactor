package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.in.FailReservationUseCase;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.ReservationStatus;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

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
        log.debug("Ejecutando failReservations en Instant: {}", expirationThreshold);

        List<String> statuses = List.of(ReservationStatus.CREATED.name(), ReservationStatus.PENDING.name());
        return reservationRepository.findReservationsBefore(expirationThreshold, statuses)
                .doOnSubscribe(__ -> log.debug("Buscando reservas vencidas en BD..."))
                .doOnNext(r -> log.debug("Reserva vencida encontrada: id={}", r.getId()))
                .doOnComplete(() -> log.debug("Búsqueda de reservas vencidas completada"))
                .flatMap(reservation -> {
                    Long reservationId = reservation.getId();
                    return reservationCache.get(reservationId)
                        .defaultIfEmpty("MISSING") // <- Valor ficticio para controlar si no hay en caché
                        .flatMap(cachedValue -> {
                            StringBuilder msg = new StringBuilder("Reserva ");
                            if ("MISSING".equals(cachedValue)) {
                                msg.append("NO ");
                            }
                            msg.append("encontrada en la caché de Redis. ")
                                    .append("TimeOut... Created At: ")
                                    .append(reservation.getCreatedAt())
                                    .append(" Time now ")
                                    .append(expirationThreshold);

                            return reservationStatusUpdater.updateStatus(
                                    reservationId,
                                    msg.toString(),
                                    ReservationStatusAction.FAILED
                            );
                        });
                })
                .then();

    }
}
