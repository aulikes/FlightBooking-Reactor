package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import com.aug.flightbooking.application.port.in.ReservationCreatedEventHandler;
import com.aug.flightbooking.infrastructure.messaging.IntegrationEventWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservationCreatedEventListenerKafka {

    private final ReservationCreatedEventHandler handler;

    @KafkaListener(
            topics = "#{@producerMessagingKafka.reservationCreatedTopic}",
            groupId = "#{@consumerMessagingKafka.reservationFlightCreatedGroupId}",
            containerFactory = "kafkaListenerContainerFactory" // Esto debe estar definido en la configuración
    )
    public void onMessage(ConsumerRecord<String, IntegrationEventWrapper<ReservationCreatedEvent>> record) {
        IntegrationEventWrapper<ReservationCreatedEvent> wrapper = record.value();
        ReservationCreatedEvent event = wrapper.data();
        handler.handle(event)
                .doOnError(e -> log.error("Error al manejar ReservationCreatedEvent", e))
                .subscribe();
    }
}
