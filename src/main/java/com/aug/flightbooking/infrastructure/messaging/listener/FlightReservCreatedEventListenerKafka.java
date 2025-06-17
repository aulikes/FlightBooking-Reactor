package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import com.aug.flightbooking.application.port.in.ReservationCreatedEventHandler;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaReceiverFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightReservCreatedEventListenerKafka {

    private final AppProperties properties;
    private final ReservationCreatedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

//    @EventListener(ApplicationReadyEvent.class)
    public void onMessage() {
        KafkaReceiverFactory
            .createReceiver(
                properties.getKafka().getProducer().getReservationCreatedTopic(),
                properties.getKafka().getConsumer().getReservationFlightCreatedGroupId(),
                properties.getKafka().getBootstrapServers()
            )
            .receive()
            .flatMap(record -> decoder.decode(record.value(), ReservationCreatedEvent.class))
            .flatMap(handler::handle)
            .doOnNext(event -> log.info("FlightReservCreatedEventListenerKafka procesado"))
            .doOnError(e -> log.error("Error procesando FlightReservCreatedEventListenerKafka", e))
            .subscribe();
    }
}
