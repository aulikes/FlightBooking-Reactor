package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import com.aug.flightbooking.application.port.out.FlightseatRejectedEventPublisher;
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
public class FlightseatRejectedEventPublisherKafka implements FlightseatRejectedEventPublisher {

    private final KafkaSender<String, byte[]> kafkaSender;
    private final AppProperties properties;
    private final ReactiveJsonEncoder encoder;

    @Override
    public Mono<Void> publish(FlightseatRejectedEvent event) {
        String key = event.getTraceId();
        String topic = properties.getKafka().getProducer().getFlightseatRejectedTopic();

        return encoder.encode(event)
                .map(payload -> {
                    ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, payload);
                    return SenderRecord.create(record, null);
                })
                .flatMap(senderRecord ->
                        kafkaSender.send(Mono.just(senderRecord)).next()
                )
                .doOnNext(result ->
                        log.info("Evento FlightseatRejected publicado correctamente: {}", key)
                )
                .doOnError(error ->
                        log.error("Error al publicar FlightseatRejected: {}", error.getMessage(), error)
                )
                .then();
    }
}
