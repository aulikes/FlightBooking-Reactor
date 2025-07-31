package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import com.aug.flightbooking.application.ports.in.ReservationCreatedEventHandler;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaReceiverFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightReservCreatedEventListenerKafka {

    private final AppProperties properties;
    private final ReservationCreatedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Flux<Void> onMessage() {
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getReservationCreatedTopic(),
                properties.getKafka().getConsumer().getReservationFlightCreatedGroupId()
        );

        return receiver.receive()
            .concatMap(record ->
                    decoder.decode(record.value(), ReservationCreatedEvent.class)
                            .flatMap(event ->
                                    handler.handle(event)
                                            .doOnSuccess(__ -> log.info("[reservation.created] Procesado OK. reservationId={}", event.reservationId()))
                            )
                            .onErrorResume(ex -> {
                                log.error("[reservation.created] Error procesando evento", ex);
                                return Mono.empty(); // no se propaga el error
                            })
                            .then(Mono.<Void>fromRunnable(() -> {
                                log.debug("[reservation.created] ACK offset={} partition={}", record.offset(), record.partition());
                                record.receiverOffset().acknowledge();
                            }))
            )
            .doOnSubscribe(sub -> log.info("FlightReservCreatedEventListenerKafka activo"))
            .doOnError(e -> log.error("[reservation.created] Error en stream principal", e));
    }
}
