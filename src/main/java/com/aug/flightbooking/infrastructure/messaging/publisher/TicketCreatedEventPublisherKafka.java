package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.events.TicketCreatedEvent;
import com.aug.flightbooking.application.ports.out.TicketCreatedEventPublisher;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
@Slf4j
public class TicketCreatedEventPublisherKafka implements TicketCreatedEventPublisher {

    private final KafkaSender<String, byte[]> kafkaSender;
    private final AppProperties.Kafka.Producer properties;
    private final ReactiveJsonEncoder encoder;

    public TicketCreatedEventPublisherKafka(KafkaSender<String, byte[]> kafkaSender,
                                            AppProperties properties, ReactiveJsonEncoder encoder) {
        this.encoder = encoder;
        this.properties = properties.getKafka().getProducer();
        this.kafkaSender = kafkaSender;
    }

    @Override
    public Mono<Void> publish(TicketCreatedEvent event) {
        String key = event.getTraceId();
        String topic = properties.getTicketCreatedTopic();
        Long reservationId = event.reservationId();

        return encoder.encode(event)
            .map(payload -> {
                ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, payload);
                return SenderRecord.create(record, null);
            })
            .flatMap(senderRecord ->
                    kafkaSender.send(Mono.just(senderRecord)).next()
            )
            .doOnNext(result ->
                    log.info("Evento TicketCreated publicado exitosamente, reservationId: {}", reservationId)
            )
            .doOnError(error ->
                    log.error("Error publicando TicketCreated: {}", error.getMessage(), error)
            )
            .then();
    }
}
