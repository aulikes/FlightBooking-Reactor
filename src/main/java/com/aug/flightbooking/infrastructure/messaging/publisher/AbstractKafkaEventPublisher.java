package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.event.IntegrationEvent;
import com.aug.flightbooking.infrastructure.messaging.IntegrationEventWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractKafkaEventPublisher<T extends IntegrationEvent> {

    private final KafkaTemplate<String, IntegrationEventWrapper<T>> kafkaTemplate;

    protected AbstractKafkaEventPublisher(KafkaTemplate<String, IntegrationEventWrapper<T>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    protected abstract String topic();

    protected String version(T event){
        return event.getVersion();
    };

    public Mono<Void> publish(T event) {
        IntegrationEventWrapper<T> wrapper = IntegrationEventWrapper.wrap(
            event,
            event.getEventType(),
            event.getVersion(),
            event.getTraceId(),
            event.getTimestamp()
        );
        log.info("Kafka - Evento publicado en [{}]: {}", topic(), event);
        return Mono.fromFuture(() -> kafkaTemplate.send(topic(), wrapper.traceId(), wrapper))
                .then();
    }
}
