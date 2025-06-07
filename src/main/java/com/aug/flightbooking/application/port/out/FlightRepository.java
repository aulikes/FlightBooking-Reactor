package com.aug.flightbooking.application.port.out;

import com.aug.flightbooking.domain.model.flight.Flight;
import com.aug.flightbooking.domain.model.reservation.Reservation;
import reactor.core.publisher.Mono;

public interface FlightRepository {
    Mono<Flight> findById(Long flightId);
    Mono<Flight> save(Flight flight);
}
