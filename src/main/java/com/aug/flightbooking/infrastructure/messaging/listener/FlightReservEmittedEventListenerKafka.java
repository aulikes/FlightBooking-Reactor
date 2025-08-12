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

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightReservEmittedEventListenerKafka {

    private final AppProperties properties;
    private final ReservationEmittedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Flux<Void> onMessage() {
        // Creamos el receptor Kafka usando configuración centralizada
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getReservationEmittedTopic(),
                properties.getKafka().getConsumer().getReservationFlightEmittedGroupId()
        );

        return receiver.receive()
            .flatMap(record ->
                // Deserializamos el mensaje del topic a un objeto ReservationEmittedEvent
                decoder.decode(record.value(), ReservationEmittedEvent.class)
                    .flatMap(event ->
                        handler.handle(event)
                            .doOnSuccess(__ -> log.info("[reservation.emitted] Procesado OK. reservationId={}", event.reservationId()))
                    )
                    .onErrorResume(ex -> {
                        log.error("[reservation.emitted] Error procesando evento", ex);
                        return Mono.empty(); // no se propaga el error
                    })
                    .then(Mono.<Void>fromRunnable(() -> {
                        log.debug("[reservation.emitted] ACK offset={} partition={}", record.offset(), record.partition());
                        record.receiverOffset().acknowledge();
                    }))
            )
            // Se ejecuta una vez cuando comienza la suscripción al topic
            .doOnSubscribe(sub -> log.info("FlightReservEmittedEventListenerKafka activo"))
            // Manejo de errores a nivel de flujo completo
            .doOnError(e -> log.error("[reservation.emitted] Error en stream principal", e));
    }
}
