package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import reactor.core.publisher.Mono;

public interface ReservationEventPublisher {

    Mono<Void> publishCreated(ReservationCreatedEvent event);
}
