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
    private final ReservFlightseatConfirmedEventListenerKafka confirmedListener;
    private final ReservFlightseatRejectedEventListenerKafka rejectedListener;

    public Mono<Void> startAllListeners() {
        return Mono.when(
            flightCreatedListener.onMessage(),
            confirmedListener.onMessage(),
            rejectedListener.onMessage()
        ).doOnSuccess(v -> log.info("Todos los listeners han sido activados"));
    }
}

