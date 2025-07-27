package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.ports.in.ReservationEmittedEventHandler;
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

    @Override
    public Mono<Ticket> handle(ReservationEmittedEvent event) {

        return ticketRepository.findByReservationId(event.reservationId())
            .switchIfEmpty(
                // Si no existe, lo creamos
                ticketRepository.save(Ticket.create(event.reservationId()))
            )
            .onErrorResume(ex -> {
                // Manejo de errores con logging diferenciado
                if (ex instanceof IllegalArgumentException) {
                    log.warn("Negocio: {}", ex.getMessage());
                } else {
                    log.error("Técnico: ", ex);
                }
                return Mono.empty(); // O podrías retornar Mono.error(ex) si quieres propagarlo
            });
    }
}
