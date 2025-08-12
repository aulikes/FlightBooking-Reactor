package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.ports.in.FlightseatConfirmedEventHandler;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaReceiverFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservFlightseatConfirmedEventListenerKafka {

    private final AppProperties properties;
    private final FlightseatConfirmedEventHandler handler;
    private final ReactiveJsonDecoder decoder;
    private final KafkaDlqPublisher dlqPublisher;

    public Flux<Void> onMessage() {
        String topic = properties.getKafka().getProducer().getFlightseatConfirmedTopic();
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                topic,
                properties.getKafka().getConsumer().getFlightseatReservationConfirmedGroupId()
        );

        return receiver.receive()
            .concatMap(record -> // uno a uno, con orden garantizado
                // 1) Decode
                decoder.decode(record.value(), FlightseatConfirmedEvent.class)
                    // 2) Handler con reintentos (solo handler)
                    .flatMap(event ->
                        Mono.defer(() -> handler.handle(event)) // re-invoca en cada retry
                            .doOnSuccess(__ ->
                                log.info("[flightseat.confirmed] Procesado OK. reservationId={}", event.reservationId())
                            )
                            .retryWhen(
                                Retry.fixedDelay(3, Duration.ofMillis(500))
                                    .onRetryExhaustedThrow((spec, sig) -> sig.failure())
                            )
                            // 3) Éxito final => ACK
                            .then(Mono.<Void>fromRunnable(() -> {
                                log.debug("[flightseat.confirmed] ACK offset={} partition={}", record.offset(), record.partition());
                                record.receiverOffset().acknowledge();
                            }))
                            // 4) Falló tras reintentos => DLQ y luego ACK
                            .onErrorResume(ex ->
                                dlqPublisher.sendToDlq(topic, record.value())
                                    .then(Mono.fromRunnable(() -> {
                                        log.error("[flightseat.confirmed] Falló tras reintentos, enviado a DLQ. ACK original", ex);
                                        record.receiverOffset().acknowledge();
                                    }))
                            )
                    )
                    // 5) Si el decode falla, también va a DLQ y luego ACK
                    .onErrorResume(ex ->
                        dlqPublisher.sendToDlq(topic, record.value())
                            .then(Mono.fromRunnable(() -> {
                                log.error("[flightseat.confirmed] Decode falló, enviado a DLQ. ACK original", ex);
                                record.receiverOffset().acknowledge();
                            }))
                    )
            )
            .doOnSubscribe(sub -> log.info("ReservFlightseatConfirmedEventListenerKafka activo"))
            .doOnError(e -> log.error("[flightseat.confirmed] Error en stream principal", e));
    }
}

