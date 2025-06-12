package com.aug.flightbooking.application.port.in;

import com.aug.flightbooking.application.command.CreateCheckInCommand;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para ejecutar el check-in de un tiquete.
 */
public interface CheckInTicketUseCase {
    Mono<Void> checkIn(CreateCheckInCommand command);
}