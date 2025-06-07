package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.domain.model.reservation.Reservation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface ReservationRepository {
    Mono<Reservation> save(Reservation reservation);
    Mono<Reservation> findById(Long id);
    Flux<Reservation> findReservationsCreatedBefore(Instant threshold);
}
