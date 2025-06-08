package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import com.aug.flightbooking.application.port.in.ReservationCreatedEventHandler;
import com.aug.flightbooking.application.port.out.FlightEventPublisher;
import com.aug.flightbooking.application.port.out.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;

/**
 * Procesa ReservationCreatedEvent desde Reservation.
 * Verifica si el vuelo tiene cupos y publica el evento correspondiente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCreatedEventHandlerService implements ReservationCreatedEventHandler {

    private final FlightRepository flightRepository;
    private final FlightEventPublisher eventPublisher;

    @Override
    public Mono<Void> handle(ReservationCreatedEvent event) {
        return flightRepository.findById(event.flightId())
                .flatMap(flight -> {
                    if (flight.tryReserveSeat()) {
                        // Encadena el guardado y el publish, ambos reactivos
                        return flightRepository.save(flight)
                                .then(eventPublisher.publishConfirmed(
                                        new FlightseatConfirmedEvent(event.reservationId())));
                    } else {
                        // Solo publica el rechazo
                        return eventPublisher.publishRejected(
                                new FlightseatRejectedEvent(event.reservationId(), "No Seat"));
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Flight Not Found")))
                .onErrorResume(ex -> {
                    log.error(ex.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
