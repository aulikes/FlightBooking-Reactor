package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.port.in.FlightseatConfirmedEventHandler;
import com.aug.flightbooking.infrastructure.messaging.IntegrationEventWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightseatConfirmedEventListenerKafka {

    private final FlightseatConfirmedEventHandler handler;

    @KafkaListener(
            topics = "#{@producerMessagingKafka.flightseatConfirmedTopic}",
            groupId = "#{@consumerMessagingKafka.flightseatReservationConfirmedGroupId}",
            containerFactory = "kafkaListenerContainerFactory" // Esto debe estar definido en la configuración
    )
    public void onMessage(ConsumerRecord<String, IntegrationEventWrapper<FlightseatConfirmedEvent>> record) {
        IntegrationEventWrapper<FlightseatConfirmedEvent> wrapper = record.value();
        FlightseatConfirmedEvent event = wrapper.data();
        handler.handle(event)
                .doOnError(e -> log.error("Error al manejar FlightseatConfirmedEvent", e))
                .subscribe();
    }
}
