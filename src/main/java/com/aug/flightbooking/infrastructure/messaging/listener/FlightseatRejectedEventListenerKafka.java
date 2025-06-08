package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import com.aug.flightbooking.application.port.in.FlightseatRejectedEventHandler;
import com.aug.flightbooking.infrastructure.messaging.IntegrationEventWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightseatRejectedEventListenerKafka {

    private final FlightseatRejectedEventHandler handler;

    @KafkaListener(
            topics = "#{@producerMessagingKafka.flightseatRejectedTopic}",
            groupId = "#{@consumerMessagingKafka.flightseatReservationRejectedGroupId}",
            containerFactory = "kafkaListenerContainerFactory" // Esto debe estar definido en la configuración
    )
    public void onMessage(ConsumerRecord<String, IntegrationEventWrapper<FlightseatRejectedEvent>> record) {
        IntegrationEventWrapper<FlightseatRejectedEvent> wrapper = record.value();
        FlightseatRejectedEvent event = wrapper.data();
        handler.handle(event)
                .doOnError(e -> log.error("Error al manejar FlightseatRejectedEvent", e))
                .subscribe();
    }
}
