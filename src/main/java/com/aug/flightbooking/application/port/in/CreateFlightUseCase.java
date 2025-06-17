package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.command.CreateFlightCommand;
import com.aug.flightbooking.domain.model.flight.Flight;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para crear vuelos.
 */
public interface CreateFlightUseCase {
    Mono<Flight> create(CreateFlightCommand command);
}
