package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.events.TicketCreatedEvent;
import com.aug.flightbooking.application.ports.in.ReservationEmittedEventHandler;
import com.aug.flightbooking.application.ports.out.TicketCreatedEventPublisher;
import com.aug.flightbooking.application.ports.out.TicketRepository;
import com.aug.flightbooking.domain.models.ticket.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Procesa ReservationCreatedEvent desde Reservation.
 * Verifica si el vuelo tiene cupos y publica el evento correspondiente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEmittedEventHandlerService implements ReservationEmittedEventHandler {

    private final TicketRepository ticketRepository;
    private final TicketCreatedEventPublisher publisher;

    @Override
    public Mono<Ticket> handle(ReservationEmittedEvent event) {
        return ticketRepository.findByReservationId(event.reservationId())
            .switchIfEmpty(
                Mono.defer(() -> {
                    Ticket ticket = Ticket.create(event.reservationId());
                    return ticketRepository.save(ticket)
                        .flatMap(saved -> publisher
                            .publish(new TicketCreatedEvent(saved.getReservationId(), "OK"))
                            .thenReturn(saved));
                })
            )
            .onErrorResume(ex -> {
                log.error("[handle][reservationId={}] Técnico: ", event.reservationId(), ex);
                return Mono.empty(); // O Mono.error(ex) si quieres que el error burbujee
            });
    }

}
