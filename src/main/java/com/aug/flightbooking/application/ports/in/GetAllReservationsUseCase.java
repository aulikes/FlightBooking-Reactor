package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.domain.models.reservation.Reservation;
import reactor.core.publisher.Flux;

public interface GetAllReservationsUseCase {
    Flux<Reservation> getAllReservations();
}
