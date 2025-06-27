package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import com.aug.flightbooking.application.port.in.ReservationCreatedEventHandler;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaReceiverFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightReservCreatedEventListenerKafka {

    private final AppProperties properties;
    private final ReservationCreatedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Mono<Void> onMessage() {
        // Creamos el receptor Kafka usando configuración centralizada
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getReservationCreatedTopic(),
                properties.getKafka().getConsumer().getReservationFlightCreatedGroupId()
        );

        return receiver.receive()
            .flatMap(record ->
                // Deserializamos el mensaje del topic a un objeto ReservationCreatedEvent
                decoder.decode(record.value(), ReservationCreatedEvent.class)
                    // Ejecutamos la lógica de dominio para procesar la reserva
                    .flatMap(handler::handle)
                    // Registramos éxito del procesamiento
                    .doOnSuccess(ok ->
                            log.info("Evento procesado correctamente. reservationId={}", record.key())
                    )
                    // Solo después de procesar con éxito, confirmamos el offset
                    .then(Mono.fromRunnable(record.receiverOffset()::acknowledge))
            )
            // Se ejecuta una vez cuando comienza la suscripción al topic
            .doOnSubscribe(sub -> log.info("FlightReservCreatedEventListenerKafka activo"))
            // Manejo de errores a nivel de flujo completo
            .doOnError(e -> log.error("Error procesando evento en FlightReservCreatedEventListenerKafka", e))
            // Convertimos a Mono<Void> para cumplir contrato del orquestador
            .then();
    }
}
