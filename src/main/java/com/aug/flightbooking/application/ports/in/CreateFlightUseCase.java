package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.application.commands.CreateFlightCommand;
import com.aug.flightbooking.domain.model.flight.Flight;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para crear vuelos.
 */
public interface CreateFlightUseCase {
    Mono<Flight> create(CreateFlightCommand command);
}
