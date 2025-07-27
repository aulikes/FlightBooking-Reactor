package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.ports.in.ReservationEmittedEventHandler;
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
public class FlightReservEmittedEventListenerKafka {

    private final AppProperties properties;
    private final ReservationEmittedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Mono<Void> onMessage() {
        // Creamos el receptor Kafka usando configuración centralizada
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getReservationEmittedTopic(),
                properties.getKafka().getConsumer().getReservationFlightEmittedGroupId()
        );

        return receiver.receive()
            .flatMap(record ->
                // Deserializamos el mensaje del topic a un objeto ReservationCreatedEvent
                decoder.decode(record.value(), ReservationEmittedEvent.class)
                    // Ejecutamos la lógica de dominio para procesar la reserva
                    .flatMap(event ->
                        handler.handle(event)
                            // Registramos éxito del procesamiento
                            .doOnSuccess(__ ->
                                log.info("Evento procesado correctamente. reservationId={}", event.reservationId())
                            )
                    )
                    // Solo después de procesar con éxito, confirmamos el offset al broker
                    .then(Mono.fromRunnable(record.receiverOffset()::acknowledge))
            )
            // Se ejecuta una vez cuando comienza la suscripción al topic
            .doOnSubscribe(sub -> log.info("FlightReservEmittedEventListenerKafka activo"))
            // Manejo de errores a nivel de flujo completo
            .doOnError(e -> log.error("Error procesando evento en FlightReservEmittedEventListenerKafka", e))
            // Convertimos a Mono<Void> para cumplir contrato del orquestador
            .then();
    }
}
