package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.domain.models.reservation.Reservation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository {
    Mono<Reservation> save(Reservation reservation);
    Mono<Reservation> findById(Long id);
    Flux<Reservation> findReservationsBefore(Instant threshold, List<String> statuses);
}
