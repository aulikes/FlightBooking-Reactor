package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.command.CreateCheckInCommand;
import com.aug.flightbooking.application.port.in.CheckInTicketUseCase;
import com.aug.flightbooking.application.port.out.TicketRepository;
import com.aug.flightbooking.domain.model.ticket.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Caso de uso para realizar el check-in de un tiquete.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CheckInTicketService implements CheckInTicketUseCase {

    private final TicketRepository ticketRepository;

    @Override
    public Mono<Void> checkIn(CreateCheckInCommand command) {
        return ticketRepository.findById(command.ticketId())
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Ticket no encontrado: " + command.ticketId())))
            .flatMap(ticket -> Mono.defer(() -> {
                try {
                    Instant departureTime = Instant.ofEpochMilli(command.millisecondInstant());
                    ticket.attemptCheckIn(departureTime, Instant.now());
                    return ticketRepository.save(ticket).then();
                } catch (Exception ex) {
                    log.warn("Check-in inválido para ticket {}: {}", ticket.getId(), ex.getMessage());
                    return Mono.error(ex);
                }
            }));
    }
}