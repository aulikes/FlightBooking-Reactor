package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.domain.model.flight.Flight;
import reactor.core.publisher.Mono;

public interface FlightRepository {
    Mono<Flight> findById(Long flightId);
    Mono<Flight> save(Flight flight);
}
