package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.ports.in.FlightseatRejectedEventHandler;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaReceiverFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservFlightseatRejectedEventListenerKafka {

    private final AppProperties properties;
    private final FlightseatRejectedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Mono<Void> onMessage() {
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getFlightseatRejectedTopic(),
                properties.getKafka().getConsumer().getFlightseatReservationRejectedGroupId()
        );

        return receiver.receive()
                .concatMap(record ->
                        decoder.decode(record.value(), FlightseatRejectedEvent.class)
                                .flatMap(event ->
                                        handler.handle(event)
                                                .doOnSuccess(__ -> log.info(
                                                        "[flightseat.rejected] Procesado OK reservationId={}",
                                                        event.reservationId()
                                                ))
                                )
                                .onErrorResume(ex -> {
                                    log.error("[flightseat.rejected] Error procesando evento", ex);
                                    return Mono.empty();
                                })
                                .then(Mono.defer(() -> {
                                    log.info("[flightseat.rejected] ACK offset={} partition={}", record.offset(), record.partition());
                                    record.receiverOffset().acknowledge();
                                    return Mono.empty();
                                }))
                )
                .doOnSubscribe(sub -> log.info("ReservFlightseatRejectedEventListenerKafka activo"))
                .doOnError(e -> log.error("[flightseat.rejected] Error en stream principal", e))
                .then();
    }
}
