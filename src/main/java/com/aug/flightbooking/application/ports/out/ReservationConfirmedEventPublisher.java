package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.application.events.ReservationConfirmedEvent;
import reactor.core.publisher.Mono;

public interface ReservationConfirmedEventPublisher {

    Mono<Void> publish(ReservationConfirmedEvent event);
}
