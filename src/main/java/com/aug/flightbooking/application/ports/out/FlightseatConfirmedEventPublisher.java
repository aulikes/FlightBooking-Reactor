package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import reactor.core.publisher.Mono;

public interface FlightseatConfirmedEventPublisher {

    Mono<Void> publish(FlightseatConfirmedEvent event);
}
