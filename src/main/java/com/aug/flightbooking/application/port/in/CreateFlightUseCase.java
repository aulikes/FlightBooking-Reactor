package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.command.CreateFlightCommand;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para crear vuelos.
 */
public interface CreateFlightUseCase {
    Mono<Void> create(CreateFlightCommand command);
}
