package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import reactor.core.publisher.Mono;

public interface ReservationEmittedEventPublisher {

    Mono<Void> publish(ReservationEmittedEvent event);
}
