package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.port.in.FlightseatConfirmedEventHandler;
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
public class ReservFlightseatConfirmedEventListenerKafka {

    private final AppProperties properties;
    private final FlightseatConfirmedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

//    @EventListener(ApplicationReadyEvent.class)
    public void listen() {
        KafkaReceiverFactory
            .createReceiver(
                properties.getKafka().getProducer().getFlightseatConfirmedTopic(),
                properties.getKafka().getConsumer().getFlightseatReservationConfirmedGroupId(),
                properties.getKafka().getBootstrapServers()
            )
            .receive()
            .flatMap(record -> decoder.decode(record.value(), FlightseatConfirmedEvent.class))
            .flatMap(handler::handle)
            .doOnNext(event -> log.info("ReservFlightseatConfirmedEventListenerKafka procesado..."))
            .doOnError(e -> log.error("Error procesando ReservFlightseatConfirmedEventListenerKafka", e))
            .subscribe();
    }
}
