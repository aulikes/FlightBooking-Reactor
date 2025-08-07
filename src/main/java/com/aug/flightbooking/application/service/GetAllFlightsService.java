package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.in.GetAllFlightsUseCase;
import com.aug.flightbooking.application.ports.out.FlightRepository;
import com.aug.flightbooking.domain.models.flight.Flight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GetAllFlightsService implements GetAllFlightsUseCase {

    private final FlightRepository flightRepository;

    @Override
    public Flux<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
}
