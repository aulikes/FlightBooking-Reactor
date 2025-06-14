package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.event.ReservationConfirmedEvent;
import com.aug.flightbooking.application.port.out.ReservationConfirmedEventPublisher;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.messaging.IntegrationEventWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReservationConfirmedEventPublisherKafka
        extends AbstractKafkaEventPublisher<ReservationConfirmedEvent>
        implements ReservationConfirmedEventPublisher {

    private final AppProperties properties;

    public ReservationConfirmedEventPublisherKafka(AppProperties properties,
                                                   KafkaTemplate<String, IntegrationEventWrapper<ReservationConfirmedEvent>> kafkaTemplate) {
        super(kafkaTemplate);
        this.properties = properties;
    }

    @Override
    protected String topic() {
        return properties.getKafka().getProducer().getReservationCreatedTopic();
    }

    //Se usa el método de la clase abstracta, a menos que se quiera personalizar la publicación
//    @Override
//    public Mono<Void> publish(ReservationCreatedEvent event) {
//        return super.publish(event);
//    }
}
