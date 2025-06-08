package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import com.aug.flightbooking.application.port.in.ReservationCreatedEventHandler;
import com.aug.flightbooking.application.port.out.FlightseatConfirmedEventPublisher;
import com.aug.flightbooking.application.port.out.FlightseatRejectedEventPublisher;
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
    private final FlightseatConfirmedEventPublisher flightseatConfirmedEventPublisher;
    private final FlightseatRejectedEventPublisher flightseatRejectedEventPublisher;

    @Override
    public Mono<Void> handle(ReservationCreatedEvent event) {
        return flightRepository.findById(event.flightId())
                .flatMap(flight -> {
                    if (flight.tryReserveSeat()) {
                        // Encadena el guardado y el publish, ambos reactivos
                        return flightRepository.save(flight)
                                .then(flightseatConfirmedEventPublisher.publish(
                                        new FlightseatConfirmedEvent(event.reservationId())));
                    } else {
                        // Solo publica el rechazo
                        return flightseatRejectedEventPublisher.publish(
                                new FlightseatRejectedEvent(event.reservationId(), "No Seat"));
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Flight Not Found" + event.flightId())))
                .onErrorResume(ex -> {
                    if (ex instanceof IllegalArgumentException) {
                        log.warn("Negocio: {}", ex.getMessage());
                    } else {
                        log.error("Técnico: ", ex);
                    }
                    return Mono.empty();
                });
    }
}
