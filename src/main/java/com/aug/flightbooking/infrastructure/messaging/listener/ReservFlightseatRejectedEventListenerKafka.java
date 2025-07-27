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
        // Creamos el receptor Kafka usando configuración centralizada
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
            properties.getKafka().getBootstrapServers(),
            properties.getKafka().getProducer().getFlightseatRejectedTopic(),
            properties.getKafka().getConsumer().getFlightseatReservationRejectedGroupId()
        );

        return receiver.receive()
            .flatMap(record ->
                decoder.decode(record.value(), FlightseatRejectedEvent.class)
                    .flatMap(event ->
                        handler.handle(event)
                            .doOnSuccess(__ ->
                                log.info("ReservFlightseatRejectedEventListenerKafka procesado correctamente. reservationId={}", event.reservationId())
                            )
                    )
                    // Solo después de procesar con éxito, confirmamos el offset al broker
                    .then(Mono.fromRunnable(record.receiverOffset()::acknowledge))
            )
            // Se ejecuta una vez cuando comienza la suscripción al topic
            .doOnSubscribe(sub -> log.info("ReservFlightseatRejectedEventListenerKafka activo"))
            .doOnError(e -> log.error("Error procesando ReservFlightseatRejectedEventListenerKafka", e))
            .then();
    }
}
