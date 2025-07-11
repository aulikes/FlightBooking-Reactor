package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import reactor.core.publisher.Mono;

public interface FlightseatRejectedEventPublisher {

    Mono<Void> publish(FlightseatRejectedEvent event);
}
