package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.TicketCreatedEvent;
import com.aug.flightbooking.application.ports.in.ReservationConfirmedEventHandler;
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
public class ReservTicketCreatedEventListenerKafka {

    private final AppProperties properties;
    private final ReservationConfirmedEventHandler handler;
    private final ReactiveJsonDecoder decoder;

    public Flux<Void> onMessage() {
        // Creamos el receptor Kafka usando configuración centralizada
        KafkaReceiver<String, byte[]> receiver = KafkaReceiverFactory.createReceiver(
                properties.getKafka().getBootstrapServers(),
                properties.getKafka().getProducer().getTicketCreatedTopic(),
                properties.getKafka().getConsumer().getTicketReservationCreatedGroupId()
        );

        return receiver.receive()
            .flatMap(record ->
                // Deserializamos el mensaje del topic a un objeto ReservationCreatedEvent
                decoder.decode(record.value(), TicketCreatedEvent.class)
                    .flatMap(event ->
                        handler.handle(event)
                            .doOnSuccess(__ -> log.info(
                                "[ticket.created] Procesado OK reservationId={}", event.reservationId()
                            ))
                    )
                    .onErrorResume(ex -> {
                        log.error("[ticket.created] Error procesando evento", ex);
                        return Mono.empty();
                    })
                    .then(Mono.<Void>fromRunnable(() -> {
                        log.debug("[ticket.created] ACK offset={} partition={}", record.offset(), record.partition());
                        record.receiverOffset().acknowledge();
                    }))
            )
            // Se ejecuta una vez cuando comienza la suscripción al topic
            .doOnSubscribe(sub -> log.info("ReservTicketCreatedEventListenerKafka activo"))
            // Manejo de errores a nivel de flujo completo
            .doOnError(e -> log.error("[ticket.created] Error en stream principal", e));
    }
}
