package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.events.TicketCreatedEvent;
import com.aug.flightbooking.application.ports.in.ReservationConfirmedEventHandler;
import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Procesa ReservationCreatedEvent desde Reservation.
 * Verifica si el vuelo tiene cupos y publica el evento correspondiente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationConfirmedEventHandlerService implements ReservationConfirmedEventHandler {

    private final ReservationRepository reservationRepository;
    private final ReservationStatusUpdater reservationStatusUpdater;

    @Override
    public Mono<Void> handle(TicketCreatedEvent event) {
        // Generar número entre 0 y 99
        int random = ThreadLocalRandom.current().nextInt(100);

        // Si está en el % inicial, no hace nada para establecer timeout con REDIS
        if (random < 30) {
            log.info("Simulación: NO se publica ningún evento para reserva {}", event.reservationId());
            return Mono.empty();
        }

        return reservationRepository.findById(event.reservationId())
            .switchIfEmpty(Mono.defer(() -> {
                log.error("[handle] reservation NO encontrado: reservationId={}", event.reservationId());
                return Mono.empty(); // Aquí salimos si no existe el vuelo
            }))
            .flatMap(reservation -> {
                log.info("[handle] reservation encontrado: reservationId={}", event.reservationId());
                return reservationStatusUpdater.updateStatus(
                        reservation, ReservationStatusAction.CONFIRMED);
            })
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
