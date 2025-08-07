package com.aug.flightbooking.application.ports.in;

import com.aug.flightbooking.domain.models.flight.Flight;
import reactor.core.publisher.Flux;

public interface GetAllFlightsUseCase {
    Flux<Flight> getAllFlights();
}
