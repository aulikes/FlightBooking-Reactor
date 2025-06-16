package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import com.aug.flightbooking.application.port.out.ReservationCreatedEventPublisher;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCreatedEventPublisherKafka implements ReservationCreatedEventPublisher {

    private final KafkaSender<String, byte[]> kafkaSender;
    private final AppProperties properties;
    private final ReactiveJsonEncoder encoder;

    @Override
    public Mono<Void> publish(ReservationCreatedEvent event) {
        String key = event.getTraceId();
        String topic = properties.getKafka().getProducer().getReservationCreatedTopic();

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
