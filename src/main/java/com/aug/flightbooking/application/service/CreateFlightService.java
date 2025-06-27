package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.command.CreateFlightCommand;
import com.aug.flightbooking.application.port.in.CreateFlightUseCase;
import com.aug.flightbooking.application.port.out.FlightRepository;
import com.aug.flightbooking.domain.model.flight.Airline;
import com.aug.flightbooking.domain.model.flight.Flight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para crear vuelos.
 */
@Service
@RequiredArgsConstructor
public class CreateFlightService implements CreateFlightUseCase {

    private final FlightRepository flightRepository;

    @Override
    public Mono<Flight> create(CreateFlightCommand command) {

        Flight flight = Flight.create(
                new Airline(command.airlineName(), command.airlineCode()),
                command.flightCode(),
                command.origin(),
                command.destination(),
                command.totalSeats(),
                command.reservedSeats(),
                command.departureDate(),
                command.arrivalDate()
        );
        return flightRepository.save(flight);
    }
}

