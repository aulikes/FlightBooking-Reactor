package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.application.commands.CreateReservationCommand;
import com.aug.flightbooking.application.results.ReservationResult;
import reactor.core.publisher.Mono;

public interface CreateReservationUseCase {
    Mono<ReservationResult> createReservation(CreateReservationCommand command);
}

