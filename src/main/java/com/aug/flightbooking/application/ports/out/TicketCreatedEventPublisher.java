package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.application.events.TicketCreatedEvent;
import reactor.core.publisher.Mono;

public interface TicketCreatedEventPublisher {

    Mono<Void> publish(TicketCreatedEvent event);
}
