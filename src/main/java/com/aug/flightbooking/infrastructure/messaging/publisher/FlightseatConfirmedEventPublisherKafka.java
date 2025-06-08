package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.port.out.FlightseatConfirmedEventPublisher;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.messaging.IntegrationEventWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FlightseatConfirmedEventPublisherKafka
        extends AbstractKafkaEventPublisher<FlightseatConfirmedEvent>
        implements FlightseatConfirmedEventPublisher {

    private final AppProperties properties;

    public FlightseatConfirmedEventPublisherKafka(AppProperties properties,
                  KafkaTemplate<String, IntegrationEventWrapper<FlightseatConfirmedEvent>> kafkaTemplate) {
        super(kafkaTemplate);
        this.properties = properties;
    }

    @Override
    protected String topic() {
        return properties.getKafka().getProducer().getFlightseatConfirmedTopic();
    }

    //Se usa el método de la clase abstracta, a menos que se quiera personalizar la publicación
//    @Override
//    public Mono<Void> publish(ReservationCreatedEvent event) {
//        return super.publish(event);
//    }
}
