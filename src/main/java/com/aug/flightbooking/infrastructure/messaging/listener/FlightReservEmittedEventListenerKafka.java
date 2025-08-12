package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.ports.in.ReservationEmittedEventHandler;
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
public class FlightReservEmittedEventListenerKafka {

    private final AppProperties properties;
    private final ReservationEmittedEventHandler handler;
    private final ReactiveJsonDecoder decoder;
    private final KafkaDlqPublisher dlqPublisher;

    public Flux<Void> onMessage() {
        String topic = properties.getKafka().getProducer().getReservationEmittedTopic();
        // Creamos el receptor Kafka usando configuración centralizada
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
            properties.getKafka().getBootstrapServers(),
            topic,
            properties.getKafka().getConsumer().getReservationFlightEmittedGroupId()
        );

        return receiver.receive()
            .concatMap(record ->
                // 1) Decode
                decoder.decode(record.value(), ReservationEmittedEvent.class)
                    // 2) Handler con reintentos (solo handler)
                    .flatMap(event ->
                        Mono.defer(() -> handler.handle(event)) // re-invoca en cada retry
                            .doOnSuccess(__ ->
                                log.info("[reservation.emitted] Procesado OK. reservationId={}", event.reservationId())
                            )
                            .retryWhen(
                                Retry.fixedDelay(3, Duration.ofMillis(500))
                                        .onRetryExhaustedThrow((spec, sig) -> sig.failure())
                            )
                            // 3) Éxito final => ACK
                            .then(Mono.<Void>fromRunnable(() -> {
                                log.debug("[reservation.emitted] ACK offset={} partition={}", record.offset(), record.partition());
                                record.receiverOffset().acknowledge();
                            }))
                            // 4) Falló tras reintentos => DLQ y luego ACK
                            .onErrorResume(ex ->
                                dlqPublisher.sendToDlq(topic, record.value())
                                    .then(Mono.fromRunnable(() -> {
                                        log.error("[reservation.emitted] Falló tras reintentos, enviado a DLQ. ACK original", ex);
                                        record.receiverOffset().acknowledge();
                                    }))
                            )
                    )
                    // 5) Si el decode falla, también va a DLQ y luego ACK
                    .onErrorResume(ex ->
                        dlqPublisher.sendToDlq(topic, record.value())
                            .then(Mono.fromRunnable(() -> {
                                log.error("[reservation.emitted] Decode falló, enviado a DLQ. ACK original", ex);
                                record.receiverOffset().acknowledge();
                            }))
                    )
            )
            // Se ejecuta una vez cuando comienza la suscripción al topic
            .doOnSubscribe(sub -> log.info("FlightReservEmittedEventListenerKafka activo"))
            // Manejo de errores a nivel de flujo completo
            .doOnError(e -> log.error("[reservation.emitted] Error en stream principal", e));
    }
}
