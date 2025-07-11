package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.domain.models.flight.Flight;
import reactor.core.publisher.Mono;

public interface FlightRepository {
    Mono<Flight> findById(Long flightId);
    Mono<Flight> save(Flight flight);
}
