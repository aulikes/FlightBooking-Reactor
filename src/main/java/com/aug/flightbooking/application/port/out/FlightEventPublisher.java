package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.application.event.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.event.FlightseatRejectedEvent;
import reactor.core.publisher.Mono;

public interface FlightEventPublisher {

    Mono<Void> publishConfirmed(FlightseatConfirmedEvent event);

    Mono<Void> publishRejected(FlightseatRejectedEvent event);
}
