package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import reactor.core.publisher.Mono;

public interface FlightseatRejectedEventPublisher {

    Mono<Void> publish(FlightseatRejectedEvent event);
}
