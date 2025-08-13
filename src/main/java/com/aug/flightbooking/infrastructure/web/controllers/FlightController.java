package com.aug.flightbooking.infrastructure.web.controllers;

import com.aug.flightbooking.application.commands.CreateFlightCommand;
import com.aug.flightbooking.application.ports.in.CreateFlightUseCase;
import com.aug.flightbooking.application.ports.in.GetAllFlightsUseCase;
import com.aug.flightbooking.infrastructure.web.dtos.FlightCreateRequest;
import com.aug.flightbooking.infrastructure.web.dtos.FlightCreateResponse;
import com.aug.flightbooking.infrastructure.web.dtos.FlightResponse;
import com.aug.flightbooking.infrastructure.web.mappers.FlightCreateMapper;
import com.aug.flightbooking.infrastructure.web.mappers.FlightResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
public class FlightController {

    private final CreateFlightUseCase createFlightUseCase;
    private final FlightCreateMapper mapper;
    private final GetAllFlightsUseCase getAllFlightsUseCase;
    private final FlightResponseMapper responseMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<FlightCreateResponse>> createFlight(@Valid @RequestBody FlightCreateRequest request) {
        CreateFlightCommand command = mapper.toCommand(request);
        return createFlightUseCase
                .create(command)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(r)))
                .defaultIfEmpty(ResponseEntity.internalServerError().build());
    }

    @GetMapping
    public Flux<FlightResponse> getAllFlights() {
        return getAllFlightsUseCase.getAllFlights()
                .map(responseMapper::toResponse);
    }
}
