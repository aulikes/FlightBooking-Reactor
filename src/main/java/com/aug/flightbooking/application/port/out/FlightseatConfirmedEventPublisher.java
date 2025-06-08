package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import reactor.core.publisher.Mono;

public interface FlightseatConfirmedEventPublisher {

    Mono<Void> publish(FlightseatConfirmedEvent event);
}
