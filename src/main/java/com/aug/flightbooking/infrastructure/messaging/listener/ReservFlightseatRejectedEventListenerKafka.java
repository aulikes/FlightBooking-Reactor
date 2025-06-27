package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import com.aug.flightbooking.application.port.in.FlightseatRejectedEventHandler;
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
public class ReservFlightseatRejectedEventListenerKafka {

    private final AppProperties properties;
    private final FlightseatRejectedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Mono<Void> onMessage() {
        return KafkaReceiverFactory
            .createReceiver(
                    properties.getKafka().getBootstrapServers(),
                    properties.getKafka().getProducer().getFlightseatRejectedTopic(),
                    properties.getKafka().getConsumer().getFlightseatReservationRejectedGroupId()
            )
            .receive()
            .flatMap(record -> decoder.decode(record.value(), FlightseatRejectedEvent.class))
            .flatMap(handler::handle)
            .doOnNext(event -> log.info("ReservFlightseatRejectedEventListenerKafka procesado"))
            .doOnError(e -> log.error("Error procesando ReservFlightseatRejectedEventListenerKafka", e))
            .then();
    }
}
