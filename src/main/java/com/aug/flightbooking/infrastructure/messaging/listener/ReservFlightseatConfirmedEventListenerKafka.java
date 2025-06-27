package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.port.in.FlightseatConfirmedEventHandler;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaReceiverFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservFlightseatConfirmedEventListenerKafka {

    private final AppProperties properties;
    private final FlightseatConfirmedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Mono<Void> onMessage() {
        return KafkaReceiverFactory
            .createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getFlightseatConfirmedTopic(),
                properties.getKafka().getConsumer().getFlightseatReservationConfirmedGroupId()
            )
            .receive()
            .flatMap(record -> decoder.decode(record.value(), FlightseatConfirmedEvent.class))
            .flatMap(handler::handle)
            .doOnNext(event -> log.info("ReservFlightseatConfirmedEventListenerKafka procesado..."))
            .doOnError(e -> log.error("Error procesando ReservFlightseatConfirmedEventListenerKafka", e))
            .then();
    }
}
