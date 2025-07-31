package com.aug.flightbooking.infrastructure.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveListenersOrchestrator {

    private final FlightReservCreatedEventListenerKafka flightCreatedListener;
    private final FlightReservEmittedEventListenerKafka flightEmittedListener;
    private final ReservFlightseatConfirmedEventListenerKafka confirmedListener;
    private final ReservFlightseatRejectedEventListenerKafka rejectedListener;
    private final ReservTicketCreatedEventListenerKafka ticketCreatedListener;

    /**
     * Método que activa todos los listeners compartidos.
     * Solo se activa cuando alguien se suscribe.
     */
    public Mono<Void> startAllListeners() {
        log.info("Activando todos los listeners reactivos...");

        Flux.merge(
                flightCreatedListener.onMessage(),
                flightEmittedListener.onMessage(),
                confirmedListener.onMessage(),
                rejectedListener.onMessage(),
                ticketCreatedListener.onMessage()
        )
        .doOnSubscribe(sub -> log.info("ReactiveListenersOrchestrator: iniciando listeners..."))
        .doOnError(e -> log.error("Error en ReactiveListenersOrchestrator", e))
        .subscribe(); //Se suscribe una sola vez, aquí

        return Mono.empty(); //Solo devuelve vacío, sin activar de nuevo
    }
}

