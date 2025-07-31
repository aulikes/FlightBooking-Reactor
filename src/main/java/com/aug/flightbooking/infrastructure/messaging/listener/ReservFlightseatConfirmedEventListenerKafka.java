package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.ports.in.FlightseatConfirmedEventHandler;
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
public class ReservFlightseatConfirmedEventListenerKafka {

    private final AppProperties properties;
    private final FlightseatConfirmedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Mono<Void> onMessage() {
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getFlightseatConfirmedTopic(),
                properties.getKafka().getConsumer().getFlightseatReservationConfirmedGroupId()
        );

        return receiver.receive()
                .concatMap(record -> // uno a uno, con orden garantizado
                        decoder.decode(record.value(), FlightseatConfirmedEvent.class)
                                .flatMap(event ->
                                        handler.handle(event)
                                                .doOnSuccess(ignored -> log.info(
                                                        "[Listener Confirmed] Procesado reservationId={}", event.reservationId()
                                                ))
                                )
                                .onErrorResume(ex -> {
                                    log.error("[Listener Confirmed] Error procesando evento", ex);
                                    return Mono.empty(); // evitar bloqueo
                                })
                                .then(Mono.defer(() -> {
                                    log.debug("[Listener Confirmed] ACK offset={} partition={}", record.offset(), record.partition());
                                    record.receiverOffset().acknowledge(); // ✅ ACK manual
                                    return Mono.empty();
                                }))
                )
                .doOnSubscribe(sub -> log.info("[Listener Confirmed] Suscrito al tópico"))
                .doOnError(e -> log.error("[Listener Confirmed] Error en el stream general", e))
                .then();
    }
}

