package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import com.aug.flightbooking.application.ports.out.ReservationCreatedEventPublisher;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaSenderFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
@Slf4j
public class ReservationCreatedEventPublisherKafka implements ReservationCreatedEventPublisher {

    private final KafkaSender<String, byte[]> kafkaSender;
    private final AppProperties.Kafka.Producer properties;
    private final ReactiveJsonEncoder encoder;

    public ReservationCreatedEventPublisherKafka(AppProperties properties, ReactiveJsonEncoder encoder) {
        this.encoder = encoder;
        this.properties = properties.getKafka().getProducer();
        this.kafkaSender = KafkaSenderFactory.createSender(properties.getKafka().getBootstrapServers());
    }

    @Override
    public Mono<Void> publish(ReservationCreatedEvent event) {
        String key = event.getTraceId();
        String topic = properties.getReservationCreatedTopic();

        return encoder.encode(event)
            .map(payload -> {
                ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, payload);
                return SenderRecord.create(record, null);
            })
            .flatMap(senderRecord ->
                    kafkaSender.send(Mono.just(senderRecord)).next()
            )
            .doOnNext(result ->
                    log.info("Evento ReservationCreated publicado exitosamente: {}", key)
            )
            .doOnError(error ->
                    log.error("Error publicando ReservationCreated: {}", error.getMessage(), error)
            )
            .then();
    }
}
