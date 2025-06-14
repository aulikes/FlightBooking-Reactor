package com.aug.flightbooking.infrastructure.web.rest;

import com.aug.flightbooking.application.command.CreateFlightCommand;
import com.aug.flightbooking.application.port.in.CreateFlightUseCase;
import com.aug.flightbooking.infrastructure.web.dto.FlightCreateRequest;
import com.aug.flightbooking.infrastructure.web.mapper.FlightCreateMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Exposición REST para la gestión de vuelos.
 */
@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
public class FlightController {

    private final CreateFlightUseCase flightService;
    private final FlightCreateMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createFlight(@Valid @RequestBody FlightCreateRequest request) {
        CreateFlightCommand command = mapper.toCommand(request);
        return flightService.create(command).then();
    }
}
