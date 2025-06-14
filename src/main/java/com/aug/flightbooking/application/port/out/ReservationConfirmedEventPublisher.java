package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.application.event.ReservationConfirmedEvent;
import reactor.core.publisher.Mono;

public interface ReservationConfirmedEventPublisher {

    Mono<Void> publish(ReservationConfirmedEvent event);
}
