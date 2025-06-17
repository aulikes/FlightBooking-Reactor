package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import com.aug.flightbooking.application.port.in.FlightseatRejectedEventHandler;
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
public class ReservFlightseatRejectedEventListenerKafka {

    private final AppProperties properties;
    private final FlightseatRejectedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

//    @EventListener(ApplicationReadyEvent.class)
    public void listen() {
        KafkaReceiverFactory
                .createReceiver(
                        properties.getKafka().getProducer().getFlightseatRejectedTopic(),
                        properties.getKafka().getConsumer().getFlightseatReservationRejectedGroupId(),
                        properties.getKafka().getBootstrapServers()
                )
                .receive()
                .flatMap(record -> decoder.decode(record.value(), FlightseatRejectedEvent.class))
                .flatMap(handler::handle)
                .doOnNext(event -> log.info("FlightseatRejectedEvent procesado"))
                .doOnError(e -> log.error("Error procesando FlightseatRejectedEvent", e))
                .subscribe();
    }
}
