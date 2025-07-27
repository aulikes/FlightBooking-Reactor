package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.ports.out.ReservationEmittedEventPublisher;
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
public class ReservationEmittedEventPublisherKafka implements ReservationEmittedEventPublisher {

    private final KafkaSender<String, byte[]> kafkaSender;
    private final AppProperties.Kafka.Producer properties;
    private final ReactiveJsonEncoder encoder;

    public ReservationEmittedEventPublisherKafka(AppProperties properties, ReactiveJsonEncoder encoder) {
        this.encoder = encoder;
        this.properties = properties.getKafka().getProducer();
        this.kafkaSender = KafkaSenderFactory.createSender(properties.getKafka().getBootstrapServers());
    }

    @Override
    public Mono<Void> publish(ReservationEmittedEvent event) {
        String key = event.getTraceId();
        Long reservationId = event.reservationId();
        String topic = properties.getReservationEmittedTopic();

        return encoder.encode(event)
            .map(payload -> {
                ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, payload);
                return SenderRecord.create(record, null);
            })
            .flatMap(senderRecord ->
                    kafkaSender.send(Mono.just(senderRecord)).next()
            )
            .doOnNext(result ->
                    log.info("Evento ReservationEmitted publicado correctamente, reservationId: {}", reservationId)
            )
            .doOnError(error ->
                    log.error("Error al publicar ReservationEmitted: {}", error.getMessage(), error)
            )
            .then();
    }
}
