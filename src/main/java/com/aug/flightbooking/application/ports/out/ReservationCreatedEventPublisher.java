package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import reactor.core.publisher.Mono;

public interface ReservationCreatedEventPublisher {

    Mono<Void> publish(ReservationCreatedEvent event);
}
