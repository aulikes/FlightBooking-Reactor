package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.command.CreateReservationCommand;
import com.aug.flightbooking.application.result.ReservationResult;
import reactor.core.publisher.Mono;

public interface CreateReservationUseCase {
    Mono<ReservationResult> createReservation(CreateReservationCommand command);
}

