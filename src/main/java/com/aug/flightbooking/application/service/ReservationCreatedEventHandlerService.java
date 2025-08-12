package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.ports.in.ReservationCreatedEventHandler;
import com.aug.flightbooking.application.ports.out.FlightseatConfirmedEventPublisher;
import com.aug.flightbooking.application.ports.out.FlightseatRejectedEventPublisher;
import com.aug.flightbooking.application.ports.out.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

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
//        // Generar número entre 0 y 99
//        int random = ThreadLocalRandom.current().nextInt(100);
//
//        // Si está en el 30% inicial, no hace nada para establecer timeout con REDIS
//        if (random < 30) {
//            log.info("Simulación ReservationCreatedEventHandlerService: NO se publica ningún evento para reserva {}", event.reservationId());
//            return Mono.empty();
//        }

        return flightRepository.findById(event.flightId())
            .switchIfEmpty(Mono.defer(() -> {
                log.error("[handle][reservationId={}] flight NO encontrado: flightId={}", event.reservationId(), event.flightId());
                return Mono.empty(); // Aquí salimos si no existe el vuelo
            }))
            .flatMap(flight -> {
                log.info("[handle][reservationId={}] flight encontrado: flightId={}", event.reservationId(), event.flightId());
                if (flight.tryReserveSeat()) {
                    return flightRepository.save(flight)
                        .then(flightseatConfirmedEventPublisher.publish(
                            new FlightseatConfirmedEvent(event.reservationId())));
                } else {
                    return flightseatRejectedEventPublisher.publish(
                        new FlightseatRejectedEvent(event.reservationId(), "No Seat"));
                }
            })
            .onErrorResume(ex -> {
                log.error("Técnico: ", ex);
                return Mono.empty();
            });
    }
}
