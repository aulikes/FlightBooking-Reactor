package com.aug.flightbooking.infrastructure.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveListenersOrchestrator {

    private final FlightReservCreatedEventListenerKafka flightCreatedListener;
    private final FlightReservEmittedEventListenerKafka flightEmittedListener;
    private final ReservFlightseatConfirmedEventListenerKafka confirmedListener;
    private final ReservFlightseatRejectedEventListenerKafka rejectedListener;

    /**
     * Método que activa todos los listeners compartidos.
     * Solo se activa cuando alguien se suscribe.
     */
    public Mono<Void> startAllListeners() {
        log.info("Activando todos los listeners reactivos...");
        return Mono.when(
            flightCreatedListener.onMessage(),
            flightEmittedListener.onMessage(),
            confirmedListener.onMessage(),
            rejectedListener.onMessage()
        )
        .doOnSubscribe(sub -> log.info("ReactiveListenersOrchestrator: iniciando listeners..."))
        .doOnSuccess(v -> log.info("Todos los listeners han sido activados"))
        .cache(); // <- evita múltiples suscripciones y re-ejecuciones
    }
}

